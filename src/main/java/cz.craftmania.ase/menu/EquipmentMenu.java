package cz.craftmania.ase.menu;

import cz.craftmania.ase.PlayerEditor;
import cz.craftmania.ase.Util;
import cz.craftmania.craftcore.builders.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class EquipmentMenu {
    static String name = "ArmorStand Equipment";
    Inventory menuInv;
    ItemStack helmet, chest, pants, feetsies, rightHand, leftHand;
    private final PlayerEditor pe;
    private final ArmorStand armorstand;

    public EquipmentMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.armorstand = as;
        name = "Armorstand inventář";
        menuInv = Bukkit.createInventory(pe.getManager().getPluginHolder(), 18, name);
    }

    public static String getName() {
        return name;
    }

    private void fillInventory() {
        menuInv.clear();
        EntityEquipment equipment = armorstand.getEquipment();
        ItemStack helmet = equipment.getHelmet();
        ItemStack chest = equipment.getChestplate();
        ItemStack pants = equipment.getLeggings();
        ItemStack feetsies = equipment.getBoots();
        ItemStack rightHand = equipment.getItemInMainHand();
        ItemStack leftHand = equipment.getItemInOffHand();
        equipment.clear();

        ItemStack disabledIcon = new ItemStack(Material.BARRIER);
        ItemMeta meta = disabledIcon.getItemMeta();
        meta.setDisplayName("§cDeaktivovano"); //equipslot.msg <option>
        ArrayList<String> loreList = new ArrayList<String>();
        loreList.add(Util.encodeHiddenLore("ase icon"));
        meta.setLore(loreList);
        disabledIcon.setItemMeta(meta);

        //ItemStack helmetIcon = createIcon(Material.LEATHER_HELMET, "helm");
        ItemStack helmetIcon = new ItemBuilder(Material.LEATHER_HELMET).setName("§fHlava").hideAllFlags()
                .setLore("helm", Util.encodeHiddenLore("ase icon")).build();
        ItemStack chestIcon = new ItemBuilder(Material.LEATHER_CHESTPLATE).setName("§fTělo").hideAllFlags()
                .setLore("chest", Util.encodeHiddenLore("ase icon")).build();
        ItemStack pantsIcon = new ItemBuilder(Material.LEATHER_LEGGINGS).setName("§fKalhoty").hideAllFlags()
                .setLore("pants", Util.encodeHiddenLore("ase icon")).build();
        ItemStack feetsiesIcon = new ItemBuilder(Material.LEATHER_BOOTS).setName("§fBoty").hideAllFlags()
                .setLore("boots", Util.encodeHiddenLore("ase icon")).build();
        ItemStack rightHandIcon = new ItemBuilder(Material.WOODEN_SWORD).setName("§fPravá ruka").hideAllFlags()
                .setLore("rhand", Util.encodeHiddenLore("ase icon")).build();
        ItemStack leftHandIcon = new ItemBuilder(Material.SHIELD).setName("§fLevá ruka").hideAllFlags()
                .setLore("lhand", Util.encodeHiddenLore("ase icon")).build();
        ItemStack[] items =
                {helmetIcon, chestIcon, pantsIcon, feetsiesIcon, rightHandIcon, leftHandIcon, disabledIcon, disabledIcon, disabledIcon,
                        helmet, chest, pants, feetsies, rightHand, leftHand, disabledIcon, disabledIcon, disabledIcon
                };
        menuInv.setContents(items);
    }

    public void open() {
        fillInventory();
        pe.getPlayer().openInventory(menuInv);
    }

    public void equipArmorstand() {
        helmet = menuInv.getItem(9);
        chest = menuInv.getItem(10);
        pants = menuInv.getItem(11);
        feetsies = menuInv.getItem(12);
        rightHand = menuInv.getItem(13);
        leftHand = menuInv.getItem(14);
        armorstand.setHelmet(helmet);
        armorstand.setChestplate(chest);
        armorstand.setLeggings(pants);
        armorstand.setBoots(feetsies);
        armorstand.getEquipment().setItemInMainHand(rightHand);
        armorstand.getEquipment().setItemInOffHand(leftHand);
    }
}
