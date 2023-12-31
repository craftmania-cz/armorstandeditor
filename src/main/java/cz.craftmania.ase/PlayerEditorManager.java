package cz.craftmania.ase;

import cz.craftmania.ase.menu.ASEHolder;
import cz.craftmania.ase.menu.EditorMenu;
import cz.craftmania.ase.menu.EquipmentMenu;
import cz.craftmania.craftcore.inventory.builder.SmartInventory;
import io.github.Leonardo0013YT.UltraMinions.api.UltraMinionsAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {

    HashMap<UUID, PlayerEditor> players;
    double coarseAdj;
    double fineAdj;
    double coarseMov;
    double fineMov;
    private final Main plugin;
    private final ASEHolder pluginHolder = new ASEHolder();

    public PlayerEditorManager(Main plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        coarseAdj = Util.FULLCIRCLE / plugin.coarseRot;
        fineAdj = Util.FULLCIRCLE / plugin.fineRot;
        coarseMov = 1;
        fineMov = .03125; // 1/32nd
    }

    //Stop players from damaging armorstands with tool in their hands and then tries to edit it.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    void onArmorStandLeftClick(EntityDamageByEntityEvent e) {
        try {
            if (e.getEntity() instanceof ArmorStand) {
                ArmorStand as = (ArmorStand) e.getEntity();
                if (e.getDamager() instanceof Player) {
                    Player player = (Player) e.getDamager();
                    if (player.getInventory().getItemInMainHand().getType() == plugin.editTool) {
                        if (!as.isVisible()) {
                            player.sendMessage("§cArmorStand musi byt viditelny, aby sel upravit!");
                            e.setCancelled(true);
                            return;
                        }
                        if (as.getName().contains("{") || as.getName().contains("}")) {
                            player.sendMessage("§cTento ArmorStand nelze upravit!");
                            e.setCancelled(true);
                            return;
                        }
                        e.setCancelled(true);
                        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
                        getPlayerEditor(player.getUniqueId()).editArmorStand(as);
                    }
                }
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    void onArmorStandRightClick(PlayerInteractAtEntityEvent e) {
        try {
            Player player = e.getPlayer();
            if (e.getRightClicked() instanceof ArmorStand) {
                if (player.getInventory().getItemInMainHand().getType() == plugin.editTool) { // if holding the edit tool apply options to click armorstand
                    e.setCancelled(true);
                    ArmorStand as = (ArmorStand) e.getRightClicked();
                    if (as.getName().contains("{") || as.getName().contains("}")) {
                        player.sendMessage("§cTento ArmorStand nelze upravit!");
                        e.setCancelled(true);
                        return;
                    }
                    getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
                    getPlayerEditor(player.getUniqueId()).reverseEditArmorStand(as);
                } else if (player.getInventory().getItemInMainHand().getType() == Material.NAME_TAG) { //if the clicked an armorstand with a nametag, apply the name
                    ItemStack nameTag = player.getInventory().getItemInMainHand();
                    if (nameTag.hasItemMeta() && nameTag.getItemMeta().hasDisplayName()) {
                        ArmorStand as = (ArmorStand) e.getRightClicked();
                        if (as.getName().contains("{") || as.getName().contains("}")) {
                            player.sendMessage("§cTento ArmorStand nelze upravit!");
                            e.setCancelled(true);
                            return;
                        }
                        if (as.getName().contains("{") || as.getName().contains("}")) {
                            player.sendMessage("§cTento ArmorStand nelze upravit!");
                            e.setCancelled(true);
                            return;
                        }
                        String name = nameTag.getItemMeta().getDisplayName();
                        name = name.replace('&', ChatColor.COLOR_CHAR);
                        if ((as.getCustomName() != null && !as.getCustomName().equals(name)) // armorstand has name and that name is not the same as the nametag
                                || (as.getCustomName() == null && (!name.equals("")))) { // neither the armorstand or the nametag have names

                            e.setCancelled(true); //nametag NOT given to armorstand
                            as.setCustomName(name);
                            as.setCustomNameVisible(true);

                            //if not in creative mode, consume a nametag
                            if (!(player.getGameMode() == GameMode.CREATIVE)) {
                                if (nameTag.getAmount() > 1) {
                                    nameTag.setAmount(nameTag.getAmount() - 1);
                                } else {
                                    nameTag = new ItemStack(Material.AIR);
                                }
                                player.getInventory().setItemInMainHand(nameTag);
                            }

                        }
                    }
                }
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    void onRightClickTool(PlayerInteractEvent e) {
        try {
            if (e.getAction() == Action.LEFT_CLICK_AIR
                    || e.getAction() == Action.RIGHT_CLICK_AIR
                    || e.getAction() == Action.LEFT_CLICK_BLOCK
                    || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player player = e.getPlayer();
                player.getInventory().getItemInMainHand();
                if (player.getInventory().getItemInMainHand().getType() == plugin.editTool) {
                    e.setCancelled(true);
                    SmartInventory ASE_MENU = SmartInventory.builder().id("armorstandEditor").provider(new EditorMenu()).size(6, 9).title("ArmorStandEditor 2").build();
                    ASE_MENU.open(player);
                }
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    void onScrollNCrouch(PlayerItemHeldEvent e) {
        try {
            Player player = e.getPlayer();
            if (player.isSneaking()) {
                if (player.getInventory().getItem(e.getPreviousSlot()) != null
                        && player.getInventory().getItem(e.getPreviousSlot()).getType() == plugin.editTool) {
                    e.setCancelled(true);
                    if (e.getNewSlot() == e.getPreviousSlot() + 1 || (e.getNewSlot() == 0 && e.getPreviousSlot() == 8)) {
                        getPlayerEditor(player.getUniqueId()).cycleAxis(1);
                    } else {
                        if (e.getNewSlot() == e.getPreviousSlot() - 1 || (e.getNewSlot() == 8 && e.getPreviousSlot() == 0)) {
                            getPlayerEditor(player.getUniqueId()).cycleAxis(-1);
                        }
                    }
                }
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    void onPlayerMenuSelect(InventoryClickEvent e) {
        try {
            if (e.getInventory().getHolder() == null) return;
            if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
            if (e.getView().getTitle().equals(EquipmentMenu.getName())) {
                ItemStack item = e.getCurrentItem();
                if (item == null) return;
                if (item.getItemMeta() == null) return;
                if (item.getItemMeta().getLore() == null) return;
                if (item.getItemMeta().getLore().contains(Util.encodeHiddenLore("ase icon"))) {
                    e.setCancelled(true);
                }
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerMenuClose(InventoryCloseEvent e) {
        try {
            if (e.getInventory().getHolder() == null) return;
            if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
            if (e.getView().getTitle().equals(EquipmentMenu.getName())) {
                PlayerEditor pe = players.get(e.getPlayer().getUniqueId());
                pe.equipMenu.equipArmorstand();
            }
        } catch (Exception | Error exception) {
            exception.printStackTrace();
        }
    }

    //Stop tracking player when he leaves
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLogOut(PlayerQuitEvent e) {
        removePlayerEditor(e.getPlayer().getUniqueId());
    }

    public PlayerEditor getPlayerEditor(UUID uuid) {
        return players.containsKey(uuid) ? players.get(uuid) : addPlayerEditor(uuid);
    }

    PlayerEditor addPlayerEditor(UUID uuid) {
        PlayerEditor pe = new PlayerEditor(uuid, plugin);
        players.put(uuid, pe);
        return pe;
    }

    void removePlayerEditor(UUID uuid) {
        players.remove(uuid);
    }

    //returns the inventoryholder that owns all the menu inventories
    public ASEHolder getPluginHolder() {
        return pluginHolder;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSwitchHands(PlayerSwapHandItemsEvent event) {
        if (plugin.editTool != event.getOffHandItem().getType()) return; //event assumes they are already switched
        event.setCancelled(true);
        Player player = event.getPlayer();
        getPlayerEditor(event.getPlayer().getUniqueId()).setTarget(getTargets(player));
    }

    ArrayList<ArmorStand> getTargets(Player player) {
        Location eyeLaser = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        ArrayList<ArmorStand> armorStands = new ArrayList<>();

        final double STEPSIZE = .5;
        final Vector STEP = direction.multiply(STEPSIZE);
        final double RANGE = 10;
        final double LASERRADIUS = .3;
        List<Entity> nearbyEntities = player.getNearbyEntities(RANGE, RANGE, RANGE);
        if (nearbyEntities == null || nearbyEntities.isEmpty()) return null;

        for (double i = 0; i < RANGE; i += STEPSIZE) {
            List<Entity> nearby = (List<Entity>) player.getWorld().getNearbyEntities(eyeLaser, LASERRADIUS, LASERRADIUS, LASERRADIUS);
            if (!nearby.isEmpty()) {
                boolean endLoop = false;
                for (Entity e : nearby) {
                    if (e instanceof ArmorStand) {
                        armorStands.add((ArmorStand) e);
                        endLoop = true;
                    }
                }
                if (endLoop) break;

            }
            if (eyeLaser.getBlock().getType().isSolid()) break;
            eyeLaser.add(STEP);
        }

        return armorStands;
    }
}
