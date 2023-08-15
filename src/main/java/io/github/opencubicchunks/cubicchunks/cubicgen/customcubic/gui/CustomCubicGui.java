/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015-2020 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.gui;

import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.malisisText;
import static io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.MalisisGuiUtils.vanillaText;

import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.DeserializationException;
import com.google.common.eventbus.Subscribe;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.ExtraGui;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.GuiOverlay;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.NoTranslationFont;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIBorderLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIColoredPanel;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UIMultilineLabel;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UISplitLayout;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UITabbedContainer;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.gui.component.UITextFieldFixed;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.CustomGenSettingsSerialization;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.JsonObjectView;
import io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer.CustomGeneratorSettingsFixer;
import mcp.MethodsReturnNonnullByDefault;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomCubicGui extends ExtraGui {

    public static final int WIDTH_1_COL = 6;
    public static final int WIDTH_2_COL = 3;
    public static final int WIDTH_3_COL = 2;

    public static final int VERTICAL_PADDING = 30;
    public static final int HORIZONTAL_PADDING = 25;
    public static final int VERTICAL_INSETS = 2;
    public static final int HORIZONTAL_INSETS = 4;
    static final int BTN_WIDTH = 60;

    private final GuiCreateWorld parent;
    private UITabbedContainer tabs;

    private BasicSettingsTab basicSettings;
    private OreSettingsTab oreSettings;
    private LakeSettingsTab lakeSettings;
    private AdvancedTerrainShapeTab advancedterrainShapeSettings;
    //private ReplacerConfigTab advancedterrainShapeSettings;
    //private CaveConfigTab advancedterrainShapeSettings;
    //private RavineConfigTab advancedterrainShapeSettings;

    private JsonObject jsonConf;

    public CustomCubicGui(GuiCreateWorld parent) {
        super();
        this.parent = parent;
    }

    /**
     * Called before display() if this {@link MalisisGui} is not constructed yet.<br>
     * Called when Ctrl+R is pressed to rebuild the GUI.
     */
    @Override
    public void construct() {
        JsonObject conf = CustomGeneratorSettingsFixer.INSTANCE.fixJson(parent.chunkProviderSettingsJson);
        reinit(conf);
    }

    @Override public void clearScreen() {
        tabs = null;
        basicSettings = null;
        oreSettings = null;
        lakeSettings = null;
        advancedterrainShapeSettings = null;
        super.clearScreen();
    }

    public void reinit(JsonObject json) {
        clearScreen();
        this.jsonConf = json;

        JsonObjectView jsonView = JsonObjectView.of(json);

        this.basicSettings = new BasicSettingsTab(this, jsonView);
        this.advancedterrainShapeSettings = new AdvancedTerrainShapeTab(this, jsonView, () -> 63); // TODO: preview
        this.oreSettings = new OreSettingsTab(this, jsonView,
                advancedterrainShapeSettings.getExpectedBaseHeight(), advancedterrainShapeSettings.getExpectedHeightVariation());
        this.lakeSettings = new LakeSettingsTab(this, jsonView);

        tabs = makeTabContainer();
        tabs.addTab(inPanel(this, basicSettings.getContainer()), vanillaText("basic_tab_title"));
        tabs.addTab(inPanel(this, oreSettings.getContainer()), vanillaText("ores_tab_title"));
        tabs.addTab(inPanel(this, lakeSettings.getContainer()), vanillaText("lake_tab_title"));
        tabs.addTab(inPanel(this, advancedterrainShapeSettings.getContainer()), vanillaText("advanced_tab_title"));
        addToScreen(tabs);

        super.afterConstruct();
    }

    private UIContainer<?> inPanel(ExtraGui gui, UIComponent<?> comp) {
        UIColoredPanel panel = new UIColoredPanel(gui);
        panel.setSize(UIComponent.INHERITED, UIComponent.INHERITED - VERTICAL_PADDING * 2);
        panel.setPosition(0, VERTICAL_PADDING);
        panel.add(comp);
        return panel;
    }

    private UITabbedContainer makeTabContainer() {
        final int xSize = UIComponent.INHERITED - HORIZONTAL_PADDING * 2 - HORIZONTAL_INSETS * 2;
        final int ySize = VERTICAL_PADDING;
        final int xPos = HORIZONTAL_PADDING + HORIZONTAL_INSETS;
        UIButton prev = new UIButton(this, malisisText("previous_page")).setSize(BTN_WIDTH, 20);
        UIButton next = new UIButton(this, malisisText("next_page")).setSize(BTN_WIDTH, 20);

        UIMultilineLabel label = new UIMultilineLabel(this)
                .setTextAnchor(Anchor.CENTER)
                .setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build());

        UIBorderLayout upperLayout = new UIBorderLayout(this)
                .setSize(xSize, ySize)
                .setPosition(xPos, 0)
                .add(prev, UIBorderLayout.Border.LEFT)
                .add(next, UIBorderLayout.Border.RIGHT)
                .add(label, UIBorderLayout.Border.CENTER);

        UIButton sharePreset = new UIButton(this, malisisText("presets")).setAutoSize(false).setSize(BTN_WIDTH, 20);
        sharePreset.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                new GuiOverlay(CustomCubicGui.this, gui -> {
                    UIButton done, cancel;
                    UITextField textMinified, textExpanded;

                    UISplitLayout<?> presetsSplit = new UISplitLayout<>(gui, UISplitLayout.Type.STACKED,
                            textMinified = new UITextFieldFixed(gui, "").setSize(0, 10),
                            textExpanded = new UITextFieldFixed(gui, "", true)
                    ).setSizeOf(UISplitLayout.Pos.FIRST, 20).setPadding(0, 3);

                    UISplitLayout<?> presetsButtonsSplit = new UISplitLayout<>(gui, UISplitLayout.Type.STACKED,
                            presetsSplit,
                            new UISplitLayout<>(gui, UISplitLayout.Type.SIDE_BY_SIDE,
                                    done = new UIButton(gui, malisisText("presets.done")).setAutoSize(false).setSize(0, 20),
                                    cancel = new UIButton(gui, malisisText("presets.cancel")).setAutoSize(false).setSize(0, 20)
                            ).setPadding(0, 3)
                    ).setSizeOf(UISplitLayout.Pos.SECOND, 26).setPadding(HORIZONTAL_PADDING, 0);

                    textMinified.register(new Object() {
                        @Subscribe
                        public void onChange(ComponentEvent.ValueChange<UITextField, String> event) {
                            float scroll = textExpanded.getOffsetY();
                            try {
                                JsonObject jsonConf = CustomGeneratorSettings.asJsonObject(event.getNewValue());
                                textExpanded.setText(getFormattedJson(jsonConf));
                                textExpanded.setOffsetY(scroll, 0);// delta doesn't appear to be used
                            } catch (Exception ex) {
                                CustomCubicMod.LOGGER.catching(ex);
                                textExpanded.setText(I18n.format("cubicgen.gui.cubicgen.presets.invalid_json"));
                            }
                        }
                    });

                    textExpanded.register(new Object() {
                        @Subscribe
                        public void onChange(ComponentEvent.ValueChange<UITextField, String> event) {
                            try {
                                JsonObject jsonConf = CustomGeneratorSettings.asJsonObject(event.getNewValue());
                                textMinified.setText(getSettingsJson(jsonConf));
                            } catch (Exception ex) {
                                CustomCubicMod.LOGGER.catching(ex);
                                textMinified.setText(I18n.format("cubicgen.gui.cubicgen.presets.invalid_json"));
                            }
                        }
                    });

                    updateConfig();

                    textExpanded.setFont(NoTranslationFont.DEFAULT);
                    textExpanded.setText(getFormattedJson(jsonConf));

                    // if we don't set the size before setting the text and jumping to the end,
                    // the end will be shown at the beginning of the
                    // textField, making the text invisible by default
                    textMinified.setSize(gui.width - HORIZONTAL_PADDING*2, 10);
                    textMinified.setFont(NoTranslationFont.DEFAULT);
                    textMinified.setText(getSettingsJson(jsonConf));
                    textMinified.getCursorPosition().jumpToEnd();

                    done.register(new Object() {
                        @Subscribe
                        public void onClick(UIButton.ClickEvent evt) {
                            try {
                                JsonObject settings = CustomGeneratorSettings.asJsonObject(textMinified.getText());
                                CustomCubicGui.this.reinit(settings);
                                mc.displayGuiScreen(CustomCubicGui.this);
                            } catch (Exception ex) {
                                CustomCubicMod.LOGGER.catching(ex);
                                done.setFontOptions(FontOptions.builder().color(0x00FF2222).build());
                            }
                        }
                    });
                    cancel.register(new Object() {
                        @Subscribe
                        public void onClick(UIButton.ClickEvent evt) {
                            mc.displayGuiScreen(CustomCubicGui.this);
                        }
                    });
                    presetsButtonsSplit.setSize(UIComponent.INHERITED, UIComponent.INHERITED);
                    return inPanel(gui, presetsButtonsSplit);
                }).guiScreenAlpha(255).display();
            }
        });
        sharePreset.setPosition(BTN_WIDTH + 10, 0);

        UIButton done = new UIButton(this, malisisText("done")).setAutoSize(false).setSize(BTN_WIDTH, 20);
        done.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                CustomCubicGui.this.done();
            }
        });
        done.setPosition(0, 0);

        UIButton populateBtn = new UIButton(this, malisisText("fill_ores")).setAutoSize(false).setSize(BTN_WIDTH, 20);
        populateBtn.register(new Object() {
            @Subscribe
            public void onClick(UIButton.ClickEvent evt) {
                CustomCubicGui.this.populateOresFromDict();
            }
        });
        populateBtn.setPosition(BTN_WIDTH * 2 + 20, 0);

        UIContainer<?> container = new UIContainer<>(this);
        container.add(done, sharePreset, populateBtn);
        container.setSize(BTN_WIDTH * 3 + 20, 20);

        UIBorderLayout lowerLayout = new UIBorderLayout(this)
                .setSize(xSize, ySize)
                .setAnchor(Anchor.BOTTOM).setPosition(xPos, 0)
                .add(container, UIBorderLayout.Border.CENTER);

        UITabbedContainer tabGroup = new UITabbedContainer(this, prev, next, label::setText);
        tabGroup.add(upperLayout, lowerLayout);

        return tabGroup;
    }

    private final int SEA_LEVEL = 42;

    private void populateOresFromDict() {
       for (String oreName : OreDictionary.getOreNames()) {
            if (!oreName.startsWith("ore")) continue;

            NonNullList<ItemStack> oreStacks = OreDictionary.getOres(oreName);
            int totalOres = oreStacks.size();

            for (ItemStack oreStack : oreStacks) {
                Item ore = oreStack.getItem();
                
                int tier = ore.getHarvestLevel(oreStack, "pickaxe", null, null) + 2;

                double factor = ( 1 - 1 / (totalOres + 1) ) * tier;

                Block block = Block.getBlockFromItem(ore);

                if (block == Blocks.AIR) {
                    continue;
                }

                String oreNameKey = block.getRegistryName().toString();

                if (oreNameKey.startsWith("tile.")) {
                    continue;
                }

                JsonObjectView toInsert = createJsonDesc(oreNameKey, 1 - 1 / factor, (int)Math.ceil(16 * factor), (int)Math.ceil(factor), tier );


                JsonObjectView.of(this.jsonConf).objectArray("standardOres").addObject(toInsert);
            }
       }
       oreSettings.draw(JsonObjectView.of(this.jsonConf));
    }

    JsonObjectView createJsonDesc(String name,double probability,int spawnSize, int spawnTries, int tier ) {
        int maxHeight = SEA_LEVEL - tier * 11; 

        return JsonObjectView.empty()
            .put("blockstate", JsonObjectView.empty().put("Name", name))
            .putNull("biomes")
            .putNull("generateWhen")
            .putNull("placeBlockWhen")
            .put("spawnSize", spawnSize)
            .put("spawnTries", spawnTries)
            .put("spawnProbability", probability)
            .put("minHeight", Double.NEGATIVE_INFINITY)
            .put("maxHeight", (double) maxHeight / 192.0); // top of the generated world

    }

    private void done() {
        updateConfig();
        parent.chunkProviderSettingsJson = getFormattedJson(jsonConf);
        close();
    }

    @Override public void close() {
        super.close();
        this.mc.displayGuiScreen(parent);
    }

    public void updateConfig() {
        JsonObjectView json = JsonObjectView.of(this.jsonConf);
        this.basicSettings.writeConfig(json);
        this.oreSettings.writeConfig(json);
        this.lakeSettings.writeConfig(json);
        this.advancedterrainShapeSettings.writeConfig(json);
    }

    String getSettingsJson(JsonObject json) {
        return json.toJson(JsonGrammar.COMPACT);
    }

    String getFormattedJson(JsonObject json) {
        return json.toJson(CustomGenSettingsSerialization.OUT_GRAMMAR);
    }

    @Deprecated // should use JsonObject directly
    public CustomGeneratorSettings getConfig() throws DeserializationException {
        updateConfig();
        return CustomGenSettingsSerialization.jankson().fromJsonCarefully(jsonConf, CustomGeneratorSettings.class);
    }
}
