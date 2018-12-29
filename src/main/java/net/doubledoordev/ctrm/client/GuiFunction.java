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

package net.doubledoordev.ctrm.client;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.doubledoordev.ctrm.CraftTweakerRecipeMaker;
import net.doubledoordev.ctrm.Helper;
import net.doubledoordev.ctrm.client.elements.GuiElement;
import net.doubledoordev.ctrm.client.elements.StringElement;
import net.doubledoordev.ctrm.xml.Function;
import net.doubledoordev.ctrm.xml.XmlParser;

/**
 * @author Dries007
 */
public class GuiFunction extends GuiListBase implements GuiElement.GuiElementCallback
{
    protected static final int ID_SAVE = 10;
    private final GuiContainer parent;
    private final Function function;
    private final String genericText;
    private String currentText;

    public GuiFunction(GuiContainer parent, Function function)
    {
        // Super??? Does something with the player inventory.
        super(parent.inventorySlots);
        // stores the passed GuiContainer
        this.parent = parent;
        // stores teh passed function
        this.function = function;
        StringBuilder textBuilder = new StringBuilder();
        // loop through functions parts
        // parts are the XML that defines how the GUI acts and the string that is added to the inputs.
        for (Object obj : function.parts)
        {
            // if the part is a string, These are the un-editable (red) part of the GUI that make up the whole script.
            if (obj instanceof String)
            {
                // add the string to the end of the text builder
                textBuilder.append(obj);
                // add a new string element to the gui list.
                guiElements.add(new StringElement(this, (String) obj));
            }
            else
            {
                // this stores the options for each part.
                XmlParser.IStringObject sObj = (XmlParser.IStringObject) obj;
                // adds the sobj to the list of parts.
                textBuilder.append(sObj.toHumanText());
                // take the object and convert it into an element to be placed in the gui
                GuiElement e = sObj.toGuiElement(this);
                // if the element is not null
                if (e != null)
                {
                    // add the element to the gui list
                    guiElements.add(e);
                }
                else
                {
                    // otherwise add a new sting element from the string object translated into people speak??? I think this is just a catch for bad entries perhaps?
                    guiElements.add(new StringElement(this, sObj.toHumanText(), 0xFF0000));
                }
            }
            // Adds a space to the end of the builder.
            textBuilder.append(' ');
        }
        // Todo: These are part of the debug
        genericText = textBuilder.toString();
        currentText = "";
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        StringBuilder sb = new StringBuilder();
        for (GuiElement obj : guiElements)
        {
            sb.append(obj.save());
        }
        // TODO: Also part of the debug
        currentText = sb.toString();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        changes = true;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // todo: remove
        // debug start
        fontRenderer.drawString("Generic", -200, -100, 0xFF5050);
        fontRenderer.drawSplitString(genericText, -200, -90, 200, 0xFFFFFF);
        fontRenderer.drawString("Current", xSize + 25, -100, 0xFF5050);
        fontRenderer.drawSplitString(currentText, xSize + 25, -90, 200, 0xFFFFFF);
        // debug end

        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        fontRenderer.drawString(function.name, guiLeft + xSize / 2 - fontRenderer.getStringWidth(function.name) / 2, guiTop - 10, 0xFFFFFF);
    }

    @Override
    protected void exit()
    {
        this.mc.displayGuiScreen(parent);
    }

    @Override
    protected void ok()
    {
        this.mc.displayGuiScreen(new GuiYesNo(this, "Does this look good?", currentText, ID_SAVE));
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        switch (id)
        {
            case ID_SAVE:
                if (result)
                {
                    confirm_ok();
                }
                else
                {
                    this.mc.displayGuiScreen(this);
                }
                break;
            default:
                super.confirmClicked(result, id);
        }
    }

    private void confirm_ok()
    {
        //todo: save currentText
        // Serverside:
        // do packet stuff
        // figure out place in file
        // add author + timestamp

        Helper.placeAfterMarker(function.weight, currentText);
        CraftTweakerRecipeMaker.log().info("Saved: {}", currentText);
        this.mc.displayGuiScreen(parent);
    }
}
