package io.github.lucaseasedup.logit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class BlockUtils
{
    private BlockUtils()
    {
    }
    
    public static Block getNearestBlockBelow(Location location)
    {
        if (location == null)
            throw new IllegalArgumentException();
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        Block block;
        
        while ((block = world.getBlockAt(x, y, z)).isEmpty())
        {
            y--;
            
            if (y < 0)
            {
                return null;
            }
        }
        
        return block;
    }
    
    public static boolean isMaterialSafe(Material material)
    {
        if (material == null)
            throw new IllegalArgumentException();
        
        return material != Material.PORTAL
                && material != Material.LAVA
                && material != Material.WATER
                && material != Material.AIR
                && material != Material.STONE_PLATE
                && material != Material.WOOD_PLATE
                && material != Material.CACTUS
                && material != Material.FIRE
                && material != Material.GOLD_PLATE
                && material != Material.IRON_PLATE
                && material != Material.TRIPWIRE;
    }
    
    public static boolean isBlockAdjacent(Block block, BlockFace face, Material... toMaterials)
    {
        if (block == null || face == null || toMaterials == null)
            throw new IllegalArgumentException();
        
        block = block.getRelative(face);
        
        for (Material toMaterial : toMaterials)
        {
            if (!block.getType().equals(toMaterial))
            {
                return false;
            }
            
            block = block.getRelative(face);
        }
        
        return true;
    }
    
    /**
     * Searches for the nearest safe location to stand at.
     * 
     * <p>{@code block} is considered safe to stand on if
     * {@code BlockUtils.isMaterialSafe(block.getType()) == true}.
     * 
     * @param origin the block around which search will start.
     * @param maxRadius maximum search radius.
     * 
     * @return the nearest safe location for a player to stand at,
     *         or {@code null} if no "safe space" was found.
     * 
     * @author     Njol
     * @modifiedby LucasEU
     */
    public static Location getNearestSafeSpace(Location origin, int maxRadius)
    {
        if (origin == null)
            throw new IllegalArgumentException();
        
        BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        BlockFace[][] orths = {
                {BlockFace.NORTH, BlockFace.EAST},
                {BlockFace.UP, BlockFace.EAST},
                {BlockFace.NORTH, BlockFace.UP}
        };
        
        for (int r = 0; r <= maxRadius; r++)
        {
            for (int i = 0; i < 6; i++)
            {
                BlockFace face = faces[i % 3];
                BlockFace[] orth = orths[i % 3];
                
                if (i >= 3)
                {
                    face = face.getOppositeFace();
                }
                
                Block referenceBlock = origin.getBlock().getRelative(face, r);
                
                for (int x = -r; x <= r; x++)
                {
                    for (int y = -r; y <= r; y++)
                    {
                        Block block = referenceBlock
                                .getRelative(orth[0], x)
                                .getRelative(orth[1], y);
                        
                        if (isMaterialSafe(block.getType()))
                        {
                            if (isBlockAdjacent(block, BlockFace.UP, Material.AIR, Material.AIR))
                            {
                                return block.getLocation().add(0.5, 1.0, 0.5);
                            }
                        }
                    }
                }
            }
       }
       
       return null;
    }
}
