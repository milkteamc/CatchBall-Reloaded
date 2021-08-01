package com.pohuang.event;

import java.util.List;
import java.util.UUID;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.pohuang.CatchBall;
import com.pohuang.ConfigSetting;
import com.pohuang.HeadDrop;
import com.pohuang.items.Ball;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.BlockProjectileSource;

import net.minecraft.nbt.NBTTagCompound;



public class HitEvent implements Listener {
    private List<EntityType> catchableEntity = ConfigSetting.catchableEntity;
    private List<UUID> ballUUID = LaunchEvent.ballUUID;
    private ItemStack catchBall = new Ball().getCatchBall();
    private Location hitLocation;
    private Plugin plugin = CatchBall.getPlugin(CatchBall.class);
    
    /* private final EntityType[] blockEntity = {EntityType.ARROW, EntityType.AREA_EFFECT_CLOUD, EntityType.MINECART_COMMAND, 
        EntityType.EGG, EntityType.DRAGON_FIREBALL, EntityType.ENDER_PEARL, EntityType.THROWN_EXP_BOTTLE , EntityType.EXPERIENCE_ORB,
        EntityType.ENDER_SIGNAL, EntityType.FALLING_BLOCK, EntityType.FIREBALL, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, 
        EntityType.DROPPED_ITEM, EntityType.THROWN_EXP_BOTTLE, EntityType.SHULKER_BULLET , EntityType.SMALL_FIREBALL , EntityType.SNOWBALL,
        EntityType.PRIMED_TNT, EntityType.TRIDENT, EntityType.PLAYER};   */

    @EventHandler
    public Boolean CatchBallHitEvent(ProjectileHitEvent event){
        
        // check if shooter is a player
        if (event.getEntity().getShooter() instanceof Player) { 
            Player player = (Player) event.getEntity().getShooter();
        
            // check if the ProJectile is catchBall
            if (!checkHasUUID(event.getEntity().getUniqueId())) { return false; } 

            event.setCancelled(true);

            event.getEntity().remove();
            // hit a entity
            if (event.getHitEntity() != null) {
                
                if (!resCheck(player, event.getHitEntity().getLocation())) { 
                    event.getHitEntity().getWorld().dropItem(event.getHitEntity().getLocation(), catchBall);
                    player.sendMessage(ConfigSetting.toChat(ConfigSetting.canNotCatchable, getCoordinate(event.getHitEntity().getLocation()), ""));
                    return false;
                } 

                Entity hitEntity = (Entity) event.getHitEntity();
                hitLocation = hitEntity.getLocation();
                net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) hitEntity).getHandle();
                String checkCustom = nmsEntity.save(new NBTTagCompound()).getString("Paper.SpawnReason");
                
                // check if the hitEntity is a catchable entity. on config.yml CatchableEntity
                for (EntityType entity : catchableEntity) {
                    if (hitEntity.getType().equals(entity) && !(hitEntity instanceof Player) && !checkCustom.equals("CUSTOM")) {
                        // hitEntity.getWorld().dropItem(hitEntity.getLocation(), entityToItemStack(entity));
                        if (!(ConfigSetting.catchSuccessSound.equals("FALSE"))) { 
                            player.playSound(player.getLocation(), Sound.valueOf(ConfigSetting.catchSuccessSound), 1f, 1f);
                        }

                        event.getHitEntity().remove();
                        hitEntity.getWorld().dropItem(hitLocation, new HeadDrop().getEntityHead(event.getHitEntity(), player));
                        
                        player.sendMessage(ConfigSetting.toChat(ConfigSetting.catchSuccess, getCoordinate(hitLocation), entity.toString()));
                        return true;
                    }             
                }
                
                // if player hit a can not be catch entity, catchBall will be return
                player.sendMessage(ConfigSetting.toChat(ConfigSetting.canNotCatchable, getCoordinate(hitLocation), ""));
                hitEntity.getWorld().dropItem(hitLocation, catchBall);   

            // hit block, catchBall will be return
            } else if (event.getHitBlock() != null) {   
                event.getEntity().remove();

                hitLocation = event.getHitBlock().getLocation();
                player.sendMessage(ConfigSetting.toChat(ConfigSetting.ballHitBlock, getCoordinate(hitLocation), ""));
                
                event.getHitBlock().getWorld().dropItem(event.getHitBlock().getLocation(), catchBall);
                return false;
            }

        } else if (event.getEntity().getShooter() instanceof BlockProjectileSource){
            if (!checkHasUUID(event.getEntity().getUniqueId())) { return false; }

            event.setCancelled(true);
            event.getEntity().remove();
            // hit a entity
            if (event.getHitEntity() != null) {
                Entity hitEntity = (Entity) event.getHitEntity();
                hitLocation = hitEntity.getLocation();
                net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) hitEntity).getHandle();
                String checkCustom = nmsEntity.save(new NBTTagCompound()).getString("Paper.SpawnReason");
                
                // check if the hitEntity is a catchable entity. on config.yml CatchableEntity
                for (EntityType entity : catchableEntity) {
                    if (hitEntity.getType().equals(entity) && !(hitEntity instanceof Player) && !checkCustom.equals("CUSTOM")) {
                        hitEntity.remove();
                        hitEntity.getWorld().dropItem(hitLocation, new HeadDrop().getEntityHead(event.getHitEntity(), null));
                        
                        return true;
                    }             
                }
                
                hitEntity.getWorld().dropItem(hitLocation, catchBall);   

            } else if (event.getHitBlock() != null) {
                hitLocation = event.getHitBlock().getLocation();
                event.getHitBlock().getWorld().dropItem(event.getHitBlock().getLocation(), catchBall);
                return false;
            }
        }

        return false;
    }

    public boolean checkHasUUID(UUID projectileUuid) {
        if (ballUUID.contains(projectileUuid)) {

            ballUUID.remove(projectileUuid);
            return true;
        }
        return false;
    }


    // config text will be use this method , so put on this class
    public String getCoordinate(Location location) {
        String xyz = String.valueOf(location.getBlockX()) + ", " +
        String.valueOf(location.getBlockY()) + ", " +
        String.valueOf(location.getBlockZ());

        return xyz;
    }

    public boolean resCheck(Player player, Location location) {
        if (plugin.getServer().getPluginManager().getPlugin("Residence") == null) { return true; }

        if (ResidenceApi.getResidenceManager().getByLoc(location) != null) {
            ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(location);
            
            for (String flags : ConfigSetting.residenceFlag) {        
                if (!residence.getPermissions().playerHas(player, Flags.valueOf(flags.toLowerCase()) , true)) {

                    player.sendMessage(ConfigSetting.toChat(ConfigSetting.noResidencePermissions, "", "").
                    replace("{FLAG}", flags));
    
                    return false;
                }
            }
        }

        return true;
    }

}
