package twilightforest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFDimension;

import java.util.function.Function;

public class NoReturnTeleporter extends TFTeleporter {
    public NoReturnTeleporter() {
        super(false);
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel dest, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        PortalInfo pos;
        TeleporterCache cache = TeleporterCache.get(dest);

        // Scale the coords based on the dimension type coordinate_scale
        ServerLevel tfDim = dest.getServer().getLevel(TFDimension.DIMENSION_KEY);
        double scale = tfDim == null ? 0.125D : tfDim.dimensionType().coordinateScale();
        scale = dest.dimension().equals(TFDimension.DIMENSION_KEY) ? 1F / scale : scale;
        BlockPos destPos = dest.getWorldBorder().clampToBounds(entity.blockPosition().getX() * scale, entity.blockPosition().getY(), entity.blockPosition().getZ() * scale);

        if ((pos = placeInExistingPortal(cache, dest, entity, destPos)) == null) { //This isn't necessary, but whatever, might as well be safe
            TwilightForestMod.LOGGER.debug("Did not find existing portal, making a new one.");
            pos = moveToSafeCoords(dest, entity, destPos);
            pos = placePosition(entity, dest, pos.pos);
        }

        return pos == null ? this.isVanilla() ? defaultPortalInfo.apply(dest) : new PortalInfo(entity.position(), Vec3.ZERO, entity.getYRot(), entity.getXRot()) : pos;
    }

    private static PortalInfo placePosition(Entity entity, ServerLevel world, Vec3 pos) {
        // ensure area is populated first
        loadSurroundingArea(world, pos);

        BlockPos spot = findPortalCoords(world, pos, blockPos -> isPortalAt(world, blockPos));
        String name = entity.getName().getString();

        if (spot != null) {
            TwilightForestMod.LOGGER.debug("Found existing portal for {} at {}", name, spot);
            return makePortalInfo(entity, Vec3.atCenterOf(spot.above()));
        }

        spot = findPortalCoords(world, pos, blockpos -> isIdealForPortal(world, blockpos));

        if (spot != null) {
            TwilightForestMod.LOGGER.debug("Found ideal portal spot for {} at {}", name, spot);
            return makePortalInfo(entity, Vec3.atCenterOf(spot.above()));
        }

        TwilightForestMod.LOGGER.debug("Did not find ideal portal spot, shooting for okay one for {}", name);
        spot = findPortalCoords(world, pos, blockPos -> isOkayForPortal(world, blockPos));

        if (spot != null) {
            TwilightForestMod.LOGGER.debug("Found okay portal spot for {} at {}", name, spot);
            return makePortalInfo(entity, Vec3.atCenterOf(spot.above()));
        }

        // well I don't think we can actually just return and fail here
        TwilightForestMod.LOGGER.debug("Did not even find an okay portal spot, just making a random one for {}", name);

        // adjust the portal height based on what world we're traveling to
        double yFactor = getYFactor(world);
        // modified copy of base Teleporter method:
        return makePortalInfo(entity, entity.getX(), (entity.getY() * yFactor) - 1.0, entity.getZ());
    }

    @Override
    protected BlockPos makePortalAt(Level world, BlockPos pos) {
        return pos;
    }
}
