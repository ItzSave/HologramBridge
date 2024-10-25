package gg.optimalgames.hologrambridge.connector.impl;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import gg.optimalgames.hologrambridge.connector.Connector;
import gg.optimalgames.hologrambridge.hologram.Hologram;
import gg.optimalgames.hologrambridge.hologram.VisibilityManager;
import gg.optimalgames.hologrambridge.hologram.impl.OptimalHologram;
import gg.optimalgames.hologrambridge.lines.Line;
import gg.optimalgames.hologrambridge.lines.types.TextLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class FancyHologramsImpl implements Connector {

    @Override
    public Hologram createHologram(Location location) {
        HologramManager manager = getHologramManager();

        Bukkit.getLogger().warning("[HologramBridge] Calling createHologram in FancyHologramsImpl ");

        // Generate unique hologram name using ThreadLocalRandom
        String hologramName = "holobridge-" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        TextHologramData hologramData = new TextHologramData(hologramName, location);

        // Configure hologram properties
        hologramData.setBillboard(Display.Billboard.CENTER);
        hologramData.setBackground(de.oliver.fancyholograms.api.hologram.Hologram.TRANSPARENT);
        hologramData.setTextUpdateInterval(10);
        hologramData.setPersistent(false);  // Ensure hologram is not saved persistently

        // Create the actual hologram instance
        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = manager.create(hologramData);

        fancyHologram.createHologram();

        // Add the hologram to the manager
        manager.addHologram(fancyHologram);

        // Show hologram immediately to all online players
        fancyHologram.showHologram(Bukkit.getOnlinePlayers());

        // Return the new hologram wrapped in the OptimalHologram class
        return new OptimalHologram(this, fancyHologram, location);
    }


    @Override
    public void setLine(Hologram hologram, int lineIndex, Line line) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(hologram);
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        if (line instanceof TextLine) {
            TextHologramData data = (TextHologramData) fancyHologram.getData();
            List<String> textLines = new ArrayList<>(data.getText());

            // Adjust the line list only if needed to avoid duplication
            if (lineIndex < textLines.size()) {
                textLines.set(lineIndex, ((TextLine) line).getText());
            } else {
                textLines.add(((TextLine) line).getText());
            }

            // Set the updated text
            data.setText(textLines);

            // Update visibility
            fancyHologram.showHologram(Bukkit.getOnlinePlayers());

            Bukkit.getLogger().log(Level.WARNING, textLines.toString());
        } else {
            throw new IllegalArgumentException("Only TextLine is supported currently.");
        }
    }


    @Override
    public void updateLine(Hologram hologram, int lineIndex, Line line) {
        setLine(hologram, lineIndex, line);  // Reuse setLine method for updating since it handles index logic.
    }

    @Override
    public void appendLine(Hologram hologram, Line line) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(hologram);
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        if (line instanceof TextLine) {
            TextHologramData data = (TextHologramData) fancyHologram.getData();
            List<String> textLines = new ArrayList<>(data.getText());

            // Append the new line only if it does not already exist
            String newLineText = ((TextLine) line).getText();
            if (!textLines.contains(newLineText)) {
                textLines.add(newLineText);
            }

            data.setText(textLines);

            // Refresh the visibility to players
            fancyHologram.showHologram(Bukkit.getOnlinePlayers());
        } else {
            throw new IllegalArgumentException("Only TextLine is supported currently.");
        }
    }


    @Override
    public void clearLines(Hologram hologram) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(hologram);
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        TextHologramData data = (TextHologramData) fancyHologram.getData();
        data.setText(new ArrayList<>());  // Clear all lines
    }

    @Override
    public void teleport(Hologram hologram, Location location) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(hologram);
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        fancyHologram.getDisplayEntity().teleport(location);  // Safely teleport hologram entity
    }

    @Override
    public void delete(Hologram hologram) {
        Bukkit.getLogger().log(Level.WARNING, "Calling delete method for FancyHolograms");
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(hologram);
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();

        // Hide and remove the hologram properly
        fancyHologram.hideHologram(Bukkit.getOnlinePlayers());
        fancyHologram.deleteHologram();

        // Remove from manager
        getHologramManager().removeHologram(fancyHologram);
    }

    @Override
    public void setVisibleByDefault(VisibilityManager visibilityManager, boolean visibleByDefault) {
        throw new UnsupportedOperationException("Currently unsupported for FancyHolograms connector.");
    }

    @Override
    public void showTo(VisibilityManager visibilityManager, Player player) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(visibilityManager.getHologram());
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        fancyHologram.showHologram(player);  // Show hologram to a specific player
    }

    @Override
    public void hideTo(VisibilityManager visibilityManager, Player player) {
        Optional<de.oliver.fancyholograms.api.hologram.Hologram> hologramOptional = getHologram(visibilityManager.getHologram());
        if (hologramOptional.isEmpty()) return;

        de.oliver.fancyholograms.api.hologram.Hologram fancyHologram = hologramOptional.get();
        fancyHologram.hideHologram(player);  // Hide hologram from a specific player
    }

    @Override
    public double getHeight(Hologram hologram) {
        throw new UnsupportedOperationException("Height retrieval is unsupported for FancyHolograms connector.");
    }

    private Optional<de.oliver.fancyholograms.api.hologram.Hologram> getHologram(Hologram hologram) {
        Object hologramObject = hologram.getHologramAsObject();
        if (hologramObject instanceof de.oliver.fancyholograms.api.hologram.Hologram) {
            return Optional.of((de.oliver.fancyholograms.api.hologram.Hologram) hologramObject);
        }
        return Optional.empty();
    }

    private HologramManager getHologramManager() {
        return FancyHologramsPlugin.get().getHologramManager();  // Utility to retrieve HologramManager
    }
}
