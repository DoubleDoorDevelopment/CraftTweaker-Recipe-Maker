/*
 * Copyright (c) 2015 - 2017, Dries007 & Double Door Development
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions via the Curse or CurseForge platform are not allowed without
 *   written prior approval.
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.doubledoordev.ctrm.client.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.oredict.OreDictionary;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.doubledoordev.ctrm.Helper;
import net.doubledoordev.ctrm.xml.elements.Slot;

import static net.doubledoordev.ctrm.client.GuiBase.BASE;

/**
 * @author Dries007
 */
public class SlotElement extends GuiElement
{
    protected final Slot.Type type;
    protected final boolean wildcardAllowed;
    protected final boolean metaWildcardAllowed;
    protected final boolean oredictAllowed;
    protected final boolean stacksizeAllowed;
    protected final boolean oredictRequired;

    protected ItemStack stack = ItemStack.EMPTY;
    protected ItemStack originalStack = ItemStack.EMPTY;

    protected int tickCounter;
    protected String oredict; // Current oredic entry.
    protected List<ItemStack> oredictList;
    protected ItemStack oredictPrevStack; // For looping trough the available list when the same stack is clicked twice.
    protected int[] oredictIds;
    protected int oredictIdCounter;
    protected int cycle = 0; // Holds the current display stack.
    protected NonNullList<ItemStack> display = NonNullList.create(); // List of items to display for oredic entries.

    public SlotElement(GuiElementCallback callback, boolean optional, Slot.Type type, boolean wildcardAllowed, boolean metaWildcardAllowed, boolean oredictAllowed, boolean stacksizeAllowed, boolean oredictRequired)
    {
        super(callback, optional);
        this.type = type;
        this.wildcardAllowed = wildcardAllowed;
        this.metaWildcardAllowed = metaWildcardAllowed;
        this.oredictAllowed = oredictAllowed;
        this.stacksizeAllowed = stacksizeAllowed;
        this.oredictRequired = oredictRequired;
    }

    @Override
    public void initGui()
    {
        height = 18;
        width = 18;
    }


    @Override
    public void update()
    {
        super.update();

        //TODO: This will need to be remade in 1.13...

        // If the oredic string is not null
        if (oredict != null)
        {
            // (tickcounter % 25) if zero update slot, this is the 'timer' for the cycle rate of oredict items.
            if (tickCounter % 25 == 0)
            {
                // Check that the display list is empty.
                if (display.size() <= 0)
                    // if our display list was empty we want to populate it with the oredict items from the list we got in setItemStackOrOredict
                    for (ItemStack stack : oredictList)
                    {
                        // if the oredict is using wildcard we need all sub items.
                        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                        {
                            // now we check the item for subtypes for sanity's sake.
                            Item item = stack.getItem();
                            if (item.getHasSubtypes())
                            {
                                // if we do have sub items lets get them from the creative tab because its the only way to get
                                // stuff that exists without the extra junk and save them to the display list.
                                // NOTE: THIS WILL ONLY FIND ITEMS IN THE CREATIVE TABS, search is used to get a full list, using CREATIVE_TAB_ARRAY
                                // will cause duplicates!
                                item.getSubItems(CreativeTabs.SEARCH, display);
                            }
                        }
                        else
                        {
                            // If we don't have any sub items we need to stick it into the display list.
                            display.add(stack);
                        }
                    }

                // This cycles the oredict entries.
                // First we need to make sure we don't go out of bounds.
                if (cycle < display.size())
                {
                    // then we set the stack from the display list and cycle to the next slot.
                    stack = display.get(cycle++);
                }
                else
                {
                    // if we go over the items in display we start over.
                    cycle = 0;
                    stack = display.get(cycle);
                }
            }
        }
        // add one to tick counter.
        tickCounter++;
    }


    @Override
    public String save()
    {
        // return oredict if oredict isn't null otherwise return stack.
        return (oredict != null) ? oredict : Helper.itemstackToString(stack);
    }

    // TODO: Document this?
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        mc.getTextureManager().bindTexture(BASE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (isFocused()) drawRect(posX, posY, posX + width, posY + height, 0xAAFF0000);
        drawTexturedModalRect(posX, posY, 256 - 18, 256 - 18, 18, 18);
        RenderHelper.enableGUIStandardItemLighting();
        drawItemStack(stack, posX + 1, posY + 1, null);
        RenderHelper.disableStandardItemLighting();
    }

    // TODO: Document this?
    @Override
    public void drawHover(int mouseX, int mouseY, int maxWidth, int maxHeight)
    {
        super.drawHover(mouseX, mouseY, maxWidth, maxHeight);
        GuiUtils.drawHoveringText(getHoverLines(), mouseX, mouseY, maxWidth, maxHeight, -1, mc.fontRenderer);
    }

    @Override
    protected void onClickOn(int mouseX, int mouseY, int mouseButton)
    {
        //TODO: Perhaps add another click to bypass oredict?
        super.onClickOn(mouseX, mouseY, mouseButton);
        //Get the held itemstack.
        ItemStack heldStack = mc.player.inventory.getItemStack();
        // Check to see if the held stack is 'empty'
        if (!heldStack.isEmpty())
        {
            // If held stack is not empty set the target spot to held item
            setItemStackOrOredict(heldStack);
            originalStack = mc.player.inventory.getItemStack();
        }
        // If item stack is empty and clicked with right mouse
        else if (mouseButton == 1)
        {
            // Clear the stack.
            reset();
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        return super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean isValid()
    {
        // run super and check if oredic isn't null, or stack isn't empty, or its optional?
        return super.isValid() && (oredict != null || stack != ItemStack.EMPTY || optional);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton)
    {
        super.onClick(mouseX, mouseY, mouseButton);
    }

    // Sets the value for the slot, either an items stack or oredic depending on allowed and available options.
    protected void setItemStackOrOredict(ItemStack input)
    {
        // Check the stack for zero items. (Seems unneeded?)
        if (input.getCount() == 0)
        {
            reset();
        }
        // Check to see if the slot does not allow oredic.
        else if (!oredictAllowed)
        {
            setItemStack(input);
        }
        // Check that the slot is the same and there are more oredic entries
        else if (input == oredictPrevStack && oredictIds.length != 0)
        {
            // add one to the ID counter
            oredictIdCounter++;
            // Divide counter by length and set the counter to it
            oredictIdCounter %= oredictIds.length;
            // populate oredic list with the ore name from oredictids from slot in counter.
            setOredict(OreDictionary.getOreName(oredictIds[oredictIdCounter]), input);
        }
        // if the slot is empty prep it for oredic
        else
        {
            // preping the slot for putting an oredic entry in.
            oredictPrevStack = input.copy();
            oredictIdCounter = 0;
            // returns all oredic ID's for this item.
            oredictIds = OreDictionary.getOreIDs(input.copy());
            reset();

            // If oredic isn't required and the id length is 0 set the slot to a stack.
            if (!oredictRequired && oredictIds.length == 0)
            {
                // Sets the slot to the item.
                setItemStack(input);
            }
            else
            {
                // set the oredic entry the first option.
                setOredict(OreDictionary.getOreName(oredictIds[oredictIdCounter]), input);
            }
        }
    }

    /**
     * Reset the slot
     */
    protected void reset()
    {
        tickCounter = 0;
        oredict = null;
        cycle = 0;
        display.clear();
        oredictList = null;
        stack = ItemStack.EMPTY;
        updateButtonsCallback();
    }

    // Clears and sets the itemstack in the slot.
    protected void setItemStack(ItemStack input)
    {
        if (oredictRequired)
        {
            return; // Shouldn't happen, but you never know
        }
        reset();
        stack = input.copy();
        stack.setTagCompound(null);
        if (!stacksizeAllowed)
        {
            stack.setCount(1);
        }
        updateButtonsCallback();
    }

    protected void setOredict(String value, ItemStack inputStack)
    {
        if (!oredictAllowed)
        {
            return; // Shouldn't happen, but you never know
        }
        reset();
        // fills oredictlist with the items from oredic value.
        oredictList = (OreDictionary.getOres(value, false));

        stack = inputStack;
        oredict = "<ore:" + value + ">";
        updateButtonsCallback();
    }

    //TODO: document this?
    protected void drawItemStack(ItemStack stack, int x, int y, String altText)
    {
        if (stack == null)
        {
            return;
        }
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        zLevel = 200.0F;
        RenderItem itemRender = mc.getRenderItem();
        itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        //noinspection ConstantConditions
        if (font == null)
        {
            font = mc.fontRenderer;
        }
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText);
        zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }


    // This is where the hover text is set for slots.
    protected ArrayList<String> getHoverLines()
    {
        //Array that holds the strings.
        ArrayList<String> list = new ArrayList<>();

        //All the options for the slots.
        //TODO: Make config option for changing colors?
        list.add(ChatFormatting.AQUA + "Options:");
        list.add("- Type: " + type);
        list.add("- Optional: " + optional);
        list.add("- Wildcard: " + (wildcardAllowed ? ChatFormatting.GREEN + "Allowed" : ChatFormatting.RED + "Not Allowed"));
        list.add("- Meta Wildcard: " + (metaWildcardAllowed ? ChatFormatting.GREEN + "Allowed" : ChatFormatting.RED + "Not Allowed"));
        list.add("- Ore Dictionary: " + (oredictAllowed ? ChatFormatting.GREEN + "Allowed" : ChatFormatting.RED + "Not Allowed"));
        list.add("- Stack size: " + (stacksizeAllowed ? ChatFormatting.GREEN + "Allowed" : ChatFormatting.RED + "Not Allowed"));
        list.add(ChatFormatting.AQUA + "Current value:");

        // adds the oredic value.
        if (oredict != null)
        {
            list.add(oredict);
        }
        else if (stack != null)
        {
            list.addAll(stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.ADVANCED));
        }
        else
        {
            list.add("null");
        }
        return list;
    }
}
