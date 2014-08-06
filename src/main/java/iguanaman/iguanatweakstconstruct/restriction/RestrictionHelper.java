package iguanaman.iguanatweakstconstruct.restriction;

import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.util.Log;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.tools.BowstringMaterial;
import tconstruct.library.tools.CustomMaterial;
import tconstruct.library.tools.FletchingMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IPattern;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.items.Pattern;

import java.util.*;

// todo: refactor this properly with a Map or something when i need to restrict more than vanilla
public abstract class RestrictionHelper {
    public static Map<String, ItemMeta> configNameToPattern; // holds the names that can be used in the config and maps them to item-meta combinations to retrieve the materials
    // this list contains all ALLOWED pattern - material combinations
    public static Map<ItemMeta, List<ToolMaterial>> patternMaterialLookup; // item+metadata -> List of applicable materials
    public static Map<ItemMeta, List<CustomMaterial>> patternCustomMaterialLookup; // item+metadata -> List of applicable custom materials

    static {
        configNameToPattern = new HashMap<String, ItemMeta>();
        patternMaterialLookup = new HashMap<ItemMeta, List<ToolMaterial>>();
        patternCustomMaterialLookup = new HashMap<ItemMeta, List<CustomMaterial>>();
    }


    public static boolean isRestricted(ItemStack pattern, ToolMaterial material)
    {
        boolean restricted = true;
        List<ToolMaterial> matIDs = patternMaterialLookup.get(new ItemMeta(pattern));
        if(matIDs != null)
        {
            for(ToolMaterial mat : matIDs)
                if(mat == material) {
                    restricted = false;
                    break;
                }
        }

        return restricted;
    }

    public static int getPatternIndexFromName(String name)
    {
        for(int i = 0; i < patternNames.length; i++)
            if(patternNames[i].equals(name))
                return i;

        return -1;
    }

    public static List<ToolMaterial> getPatternMaterials(ItemStack pattern)
    {
        return patternMaterialLookup.get(new ItemMeta(pattern));
    }

    public static List<CustomMaterial> getPatternCustomMaterials(ItemStack pattern)
    {
        return patternCustomMaterialLookup.get(new ItemMeta(pattern));
    }

    public static void addRestriction(ItemMeta key, ToolMaterial material)
    {
        // fetch the material list
        List<ToolMaterial> materials = patternMaterialLookup.get(key);
        if(materials == null)
        {
            Log.debug(String.format("Couldn't find lookup entry for %s:%d", key.item.getUnlocalizedName(), key.meta));
            return;
        }

        // find the entry and remove it
        ListIterator<ToolMaterial> iter = materials.listIterator();
        while(iter.hasNext())
        {
            ToolMaterial mat = iter.next();
            if(mat == material)
            {
                iter.remove();
            }
        }
    }

    // removes all recipes that use a certain tool from the list
    public static void clearRecipes(ToolMaterial material)
    {
        for(List<ToolMaterial> mats : patternMaterialLookup.values()) {
            ListIterator<ToolMaterial> iter = mats.listIterator();
            while(iter.hasNext())
            {
                ToolMaterial mat = iter.next();
                if(mat == material)
                {
                    iter.remove();
                }
            }
        }
    }

    public static void addAllowed(ItemMeta key, ToolMaterial material)
    {
        // fetch the material list
        List<ToolMaterial> materials = patternMaterialLookup.get(key);
        if(materials == null)
        {
            Log.debug(String.format("Couldn't find lookup entry for %s:%d", key.item.getUnlocalizedName(), key.meta));
            return;
        }

        // find the entry so we don't have a double entry
        ListIterator<ToolMaterial> iter = materials.listIterator();
        while(iter.hasNext())
        {
            ToolMaterial mat = iter.next();
            if(mat == material)
            {
                return;
            }
        }

        // item is not in list. add it
        materials.add(material);
    }

    public static void initPatternParts()
    {
        Log.info("Loading tool pattern combinations");
        // cycle through ALL combinations
        for(Map.Entry<List, ItemStack> entry : TConstructRegistry.patternPartMapping.entrySet())
        {
            Item pattern = (Item)entry.getKey().get(0); // the pattern
            Integer meta = (Integer)entry.getKey().get(1); // metadata of the pattern
            Integer matID = (Integer)entry.getKey().get(2); // Material-ID of the material needed to craft

            String name;
            if(pattern == TinkerTools.woodPattern && meta <= patternNames.length)
                name = patternNames[meta];
            else
                name = (new ItemStack(pattern, 1, meta)).getUnlocalizedName();

            ItemMeta im = configNameToPattern.get(name);
            // not registered in the mapping yet?
            if(im == null) {
                im = new ItemMeta(pattern, meta);
                configNameToPattern.put(name, im);
            }

            // add material
            if(!patternMaterialLookup.containsKey(im))
                patternMaterialLookup.put(im, new LinkedList<ToolMaterial>());

            patternMaterialLookup.get(im).add(TConstructRegistry.getMaterial(matID));
        }

        // bowstring and fletchling are treated differently
        for(Integer matID : TConstructRegistry.toolMaterials.keySet())
        {
            // bowstring
            CustomMaterial mat = TConstructRegistry.getCustomMaterial(matID, BowstringMaterial.class);
            if(mat != null)
            {
                ItemMeta im = configNameToPattern.get("bowstring");
                // not registered in the mapping yet?
                if(im == null) {
                    im = new ItemMeta(TinkerTools.woodPattern, 23);
                    configNameToPattern.put("bowstring", im);
                }

                // add material
                if(!patternCustomMaterialLookup.containsKey(im))
                    patternCustomMaterialLookup.put(im, new LinkedList<CustomMaterial>());

                patternCustomMaterialLookup.get(im).add(mat);
            }

            // fletchling
            mat = TConstructRegistry.getCustomMaterial(matID, FletchingMaterial.class);
            if(mat != null)
            {
                ItemMeta im = configNameToPattern.get("fletching");
                // not registered in the mapping yet?
                if(im == null) {
                    im = new ItemMeta(TinkerTools.woodPattern, 24);
                    configNameToPattern.put("fletching", im);
                }

                // add material
                if(!patternCustomMaterialLookup.containsKey(im))
                    patternCustomMaterialLookup.put(im, new LinkedList<CustomMaterial>());

                patternCustomMaterialLookup.get(im).add(mat);
            }
        }
    }

    // item + metadata combination
    public static class ItemMeta {
        public Item item;
        public Integer meta;

        public ItemMeta(Item item, Integer meta) {
            this.item = item;
            this.meta = meta;
        }

        public ItemMeta(ItemStack stack)
        {
            this.item = stack.getItem();
            this.meta = stack.getItemDamage();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemMeta itemMeta = (ItemMeta) o;

            if (item != null ? !item.equals(itemMeta.item) : itemMeta.item != null) return false;
            if (meta != null ? !meta.equals(itemMeta.meta) : itemMeta.meta != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = item != null ? item.hashCode() : 0;
            result = 31 * result + (meta != null ? meta.hashCode() : 0);
            return result;
        }
    }

    // the tool parts
    public static final String[] patternNames = new String[] {
            "ingot",        //  0
            "rod",          //  1
            "pickaxe",      //  2
            "shovel",       //  3
            "axe",          //  4
            "swordblade",   //  5
            "largeguard",   //  6
            "mediumguard",  //  7
            "crossbar",     //  8
            "binding",      //  9
            "frypan",       // 10
            "sign",         // 11
            "knifeblade",   // 12
            "chisel",       // 13
            "largerod",     // 14
            "toughbinding", // 15
            "largeplate",   // 16
            "broadaxe",     // 17
            "scythe",       // 18
            "excavator",    // 19
            "largeblade",   // 20
            "hammerhead",   // 21
            "fullguard",    // 22
            "bowstring",    // 23
            "fletching",    // 24
            "arrowhead"     // 25
    };

    public static final String[] defaultRestrictions = new String[]{
            // Wood:
            "Wood:pickaxe",
            "Wood:axe",
            "Wood:swordblade",
            "Wood:largeguard",
            "Wood:mediumguard",
            "Wood:frypan",
            "Wood:chisel",
            "Wood:largerod",
            "Wood:toughbinding",
            "Wood:largeplate",
            "Wood:broadaxe",
            "Wood:scythe",
            "Wood:excavator",
            "Wood:largeblade",
            "Wood:hammerhead",
            "Wood:fullguard",

            // Flint:
            "Flint:rod",
            "Flint:swordblade",
            "Flint:largeguard",
            "Flint:mediumguard",
            "Flint:crossbar",
            "Flint:binding",
            "Flint:frypan",
            "Flint:sign",
            "Flint:largerod",
            "Flint:toughbinding",
            "Flint:largeplate",
            "Flint:broadaxe",
            "Flint:scythe",
            "Flint:excavator",
            "Flint:largeblade",
            "Flint:hammerhead",
            "Flint:fullguard",

            // Bone
            "Bone:pickaxe",
            "Bone:swordblade",
            "Bone:largeguard",
            "Bone:mediumguard",
            "Bone:binding",
            "Bone:frypan",
            "Bone:sign",
            "Bone:chisel",
            "Bone:largerod",
            "Bone:toughbinding",
            "Bone:largeplate",
            "Bone:broadaxe",
            "Bone:scythe",
            "Bone:excavator",
            "Bone:largeblade",
            "Bone:hammerhead",
            "Bone:fullguard",

            // Cactus
            "Cactus:pickaxe",
            "Cactus:shovel",
            "Cactus:axe",
            "Cactus:swordblade",
            "Cactus:largeguard",
            "Cactus:mediumguard",
            "Cactus:crossbar",
            "Cactus:frypan",
            "Cactus:sign",
            "Cactus:knifeblade",
            "Cactus:chisel",
            "Cactus:largerod",
            "Cactus:toughbinding",
            "Cactus:largeplate",
            "Cactus:broadaxe",
            "Cactus:scythe",
            "Cactus:excavator",
            "Cactus:largeblade",
            "Cactus:hammerhead",
            "Cactus:fullguard",
            "Cactus:arrowhead",

            // Paper:
            "Paper:pickaxe",
            "Paper:shovel",
            "Paper:axe",
            "Paper:swordblade",
            "Paper:largeguard",
            "Paper:mediumguard",
            "Paper:crossbar",
            "Paper:frypan",
            "Paper:sign",
            "Paper:knifeblade",
            "Paper:chisel",
            "Paper:largerod",
            "Paper:toughbinding",
            "Paper:largeplate",
            "Paper:broadaxe",
            "Paper:scythe",
            "Paper:excavator",
            "Paper:largeblade",
            "Paper:hammerhead",
            "Paper:fullguard",
            "Paper:arrowhead",

            // Slime:
            "Slime:pickaxe",
            "Slime:shovel",
            "Slime:axe",
            "Slime:swordblade",
            "Slime:largeguard",
            "Slime:mediumguard",
            "Slime:crossbar",
            "Slime:frypan",
            "Slime:knifeblade",
            "Slime:chisel",
            "Slime:largerod",
            "Slime:toughbinding",
            "Slime:largeplate",
            "Slime:broadaxe",
            "Slime:scythe",
            "Slime:excavator",
            "Slime:largeblade",
            "Slime:hammerhead",
            "Slime:fullguard",
            "Slime:arrowhead",

            // BlueSlime:
            "BlueSlime:pickaxe",
            "BlueSlime:shovel",
            "BlueSlime:axe",
            "BlueSlime:swordblade",
            "BlueSlime:largeguard",
            "BlueSlime:mediumguard",
            "BlueSlime:crossbar",
            "BlueSlime:frypan",
            "BlueSlime:sign",
            "BlueSlime:knifeblade",
            "BlueSlime:chisel",
            "BlueSlime:largerod",
            "BlueSlime:toughbinding",
            "BlueSlime:largeplate",
            "BlueSlime:broadaxe",
            "BlueSlime:scythe",
            "BlueSlime:excavator",
            "BlueSlime:largeblade",
            "BlueSlime:hammerhead",
            "BlueSlime:fullguard",
            "BlueSlime:arrowhead"
    };

    public static final String[] defaultAllowed = new String[] {
            // Wood:
            "Wood:rod",
            "Wood:binding",
            "Wood:sign",

            // Flint:
            "Flint:pickaxe",
            "Flint:knifeblade",
            "Flint:arrowhead",

            // Bone
            "Bone:rod",
            "Bone:shovel",
            "Bone:axe",
            "Bone:knifeblade",
            "Bone:arrowhead",

            // Cactus
            "Cactus:binding",

            // Paper:
            "Paper:rod",
            "Paper:binding",

            // Slime:
            "Slime:rod",
            "Slime:sign",
            "Slime:binding",

            // BlueSlime
            "BlueSlime:rod",
            "BlueSlime:binding",
    };
}
