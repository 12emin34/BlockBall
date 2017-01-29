package com.github.shynixn.blockball.business.logic.items;

import com.github.shynixn.blockball.api.entities.items.Spawnrate;
import com.github.shynixn.blockball.lib.FastPotioneffect;
import com.github.shynixn.blockball.lib.SSKulls;
import com.github.shynixn.blockball.api.entities.items.BoostItem;
import com.github.shynixn.blockball.lib.LightPotioneffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shynixn
 */
class SpawnItem implements BoostItem {
    private static final long serialVersionUID = 1L;
    private int id = 260;
    private int damage;
    private String owner;
    private Spawnrate spawnrate = Spawnrate.MEDIUM;
    private String displayName;
    private final Map<Integer, LightPotioneffect> potioneffectList = new HashMap<>();

    SpawnItem() {
    }

    SpawnItem(Map<String, Object> items) throws Exception {
        this.id = (int) items.get("id");
        this.damage = (int) items.get("damage");
        this.owner = (String) items.get("owner");
        this.displayName = (String) items.get("name");
        this.spawnrate = Spawnrate.getSpawnrateFromName((String) items.get("rate"));
        for (final PotionEffectType potionEffectType : PotionEffectType.values()) {
            if (potionEffectType != null && items.containsKey("potioneffects." + potionEffectType.getId())) {
                this.potioneffectList.put(potionEffectType.getId(), new FastPotioneffect(((MemorySection) items.get("potioneffects." + potionEffectType.getId())).getValues(true)));
            }
        }
    }

    @Override
    public ItemStack generate() {
        ItemStack itemStack = new ItemStack(Material.getMaterial(this.id), 1, (short) this.damage);
        if (this.id == Material.SKULL_ITEM.getId() && this.damage == 3) {
            if (this.owner != null && this.owner.contains("textures.minecraft")) {
                itemStack = SSKulls.activateHeadByURL(this.owner, itemStack);
            } else if (this.owner != null) {
                itemStack = SSKulls.activateHeadByName(this.owner, itemStack);
            }
        }
        return itemStack;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
    }

    @Override
    public void apply(Player player) {
        for (final LightPotioneffect lightPotioneffect : this.potioneffectList.values()) {
            lightPotioneffect.apply(player);
        }
    }

    @Override
    public void setPotionEffect(LightPotioneffect lightPotioneffect) {
        this.potioneffectList.put(lightPotioneffect.getType(), lightPotioneffect);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        if (this.potioneffectList.containsKey(type.getId()))
            this.potioneffectList.remove(type.getId());
    }

    @Override
    public LightPotioneffect getPotionEffect(PotionEffectType type) {
        if (this.potioneffectList.containsKey(type.getId()))
            return this.potioneffectList.get(type.getId());
        return null;
    }

    @Override
    public LightPotioneffect[] getPotionEffects() {
        return this.potioneffectList.values().toArray(new LightPotioneffect[this.potioneffectList.size()]);
    }

    @Override
    public Spawnrate getSpawnrate() {
        return this.spawnrate;
    }

    @Override
    public void setSpawnrate(Spawnrate spawnrate) {
        this.spawnrate = spawnrate;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("damage", this.damage);
        map.put("owner", this.owner);
        map.put("name", this.displayName);
        map.put("rate", this.spawnrate.name().toUpperCase());
        for (final Integer integer : this.potioneffectList.keySet()) {
            map.put("potioneffects." + integer, this.potioneffectList.get(integer).serialize());
        }
        return map;
    }
}
