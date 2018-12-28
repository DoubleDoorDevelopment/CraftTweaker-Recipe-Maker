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

package net.doubledoordev.ctrm;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import net.doubledoordev.ctrm.xml.Function;
import net.doubledoordev.ctrm.xml.Root;
import net.doubledoordev.ctrm.xml.XmlParser;

/**
 * @author Dries007
 */
@SuppressWarnings("WeakerAccess")
public final class Helper
{
    public static final String MODID = "ctrm";
    public static final String NAME = "CraftTweakerRecipeMaker";
    public static final String MARKERBASE = "//#CTRM MARKER ";
    //TODO: What is this for? Is it needed?
    public static final DateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    public static TreeSet<Integer> MARKERS = new TreeSet<>();

    public static final String DTD = "/assets/ctrm/ctrm.dtd";

    public static final FileFilter FILE_FILTER_XML = pathname -> pathname.isDirectory() || FilenameUtils.getExtension(pathname.getName()).equalsIgnoreCase("XML");

    private Helper() {}

    /**
     * Gets the scripts <file>file</file>
     */
    public static File getScriptFile() throws IOException
    {
        File file = new File("scripts/", "ctrm.zs");
        Files.createParentDirs(file);
        if (!file.exists())
        {
            Files.touch(file);
            writeHeader(Files.newWriter(file, Charset.defaultCharset()), "<SERVER>").close();
        }
        return file;
    }

    /**
     * Simple method to get all the lines of the script file.
     *
     * @return the script file as a <String>List</String> or null if no lines are found.
     */
    public static List<String> getScripLines()
    {
        List<String> lines = null;
        try
        {
            // try to read the lines.
            lines = FileUtils.readLines(getScriptFile(), "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Inserts <string>scriptString</string> ALWAYS AFTER <int>markerValue</int>
     * Does not notify of success or failure.
     */
    public static void placeAfterMarker(int markerValue, String scriptString)
    {
        List<String> lines = getScripLines();
        ListIterator<String> i = lines.listIterator();
        while (i.hasNext())
        {
            String line = i.next();
            // step through each line of the script file to find the marker we need
            if (line.equals(MARKERBASE + markerValue))
            {
                // string is inserted into list, ALWAYS AFTER MARKER!
                i.add(scriptString);
                break;
            }
        }
        try
        {
            // write back to file the dumpster fire that was created.
            FileUtils.writeLines(getScriptFile(), lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the line number of a <int>markerValue</int>
     *
     * @return the list as an int, or -1 if none are found.
     */
    public static int getMarkerLine(int markerValue)
    {
        List<String> lines = getScripLines();
        ListIterator<String> i = lines.listIterator();
        while (i.hasNext())
        {
            String line = i.next();
            if (line.equals(MARKERBASE + markerValue))
            {
                // return the index of the next line -1, could be done another way I bet but this works.
                return i.nextIndex() - 1;
            }
        }
        return -1;
    }

    /**
     * Takes all loaded <class>Root</class> elements and gets all the weights
     * from the <class>Function</class>s provided.
     */
    public static void fillMarkersTreeSet()
    {
        // Get all the loaded XMLs
        for (Root roots : XmlParser.getLoadedRootXmls())
        {
            // Get all the functions, contains name an weight
            for (Function func : roots.functionList)
            {
                // populate markers with weights.
                MARKERS.add(func.weight);
            }
        }
    }

    /**
     * Inserts markers into the script file for us to organize with.
     * Will only place markers that don't exist.
     */
    public static void makeMissingMarkers()
    {
        // always start with zero, first marker is 0.
        int lastExisting = 0;

        for (int marker : MARKERS)
        {
            // check to see if the marker is in the file, -1 is a missing marker from getMarkerLine
            if (getMarkerLine(marker) == -1)
            {
                // place the marker in file at the lastexisting marker.
                placeAfterMarker(lastExisting, MARKERBASE + marker);
                // make sure to update the last marker as we may need to place after it.
                lastExisting = marker;
            }
            else
            {
                // If we find a marker we just update the last marker!
                lastExisting = marker;
            }
        }
    }

    public static BufferedWriter writeHeader(BufferedWriter br, String name) throws IOException
    {
        br.write("// File generated by CraftTweakerRecipeMaker (ctrm) by DoubleDoorDevelopment\r\n");
        br.write("// This file is automatically managed by ctrm.\r\n");
        br.write("// Try not to touch it if you don't absolutely have to.\r\n");
        br.write("// If you have too, leave the markers alone. They look like this:\r\n");
        br.write("//\r\n");
        br.write("//    #CTRM MARKER <number here>\r\n");
        br.write("//\r\n");
        br.write("// ================================================================================\r\n");
        br.write("print(\"Loading the CraftTweakerRecipeMaker (ctrm) script file.\");\r\n");
        br.write("// HERE BE DRAGONS\r\n\r\n");
        br.write("//#CTRM MARKER 0\r\n");
        return br;
    }

    /**
     * Recursive XML file finder. Always returns the given list.
     *
     * @return the list parameter
     */
    public static List<File> findXMLFiles(File folder, List<File> list)
    {
        if (!folder.isDirectory())
        {
            throw new IllegalArgumentException(folder + " is not a directory.");
        }
        //noinspection ConstantConditions
        for (File file : folder.listFiles(FILE_FILTER_XML))
        {
            if (file.isDirectory())
            {
                findXMLFiles(file, list);
            }
            else
            {
                list.add(file);
            }
        }
        return list;
    }

    public static void makeReadme(File file) throws IOException
    {
        file = new File(file, "README.txt");
        FileUtils.writeLines(file, Arrays.asList(
                "If you want to manually add or edit XML config files, put them in there like they would be in a resourcepack.",
                "This WILL override existing files if they already exist. There will be no version checking!",
                "If you make a useful change, submit it to the original authors, so everyone will benefit.",
                "~Dries007",
                "",
                "EXAMPLES:",
                "You want the edit the default vanilla XML: Put it at 'overrides/ctrm/vanilla.xml'",
                "If you want to add a new XML file, you can put it wherever inside of 'overrides', as long as it has a sub-folder.",
                "",
                "The current CTRM DTD File: (Use this to make sure your XML is correct):",
                ""
        ), "\r\n");
        InputStream is = CraftTweakerRecipeMaker.class.getResourceAsStream(DTD);
        //TODO: Fix this.
        FileUtils.writeStringToFile(file, IOUtils.toString(is).replaceAll("\\r?\\n", "\n"), true);
        is.close();
    }

    public static ResourceLocation normalize(ResourceLocation location)
    {
        String path = location.getResourcePath();
        if (path.startsWith("assets/"))
        {
            path = path.substring(path.indexOf('/'));
        }
        if (path.endsWith(".xml") || path.endsWith(".XML"))
        {
            path = path.substring(0, path.lastIndexOf('.'));
        }
        return new ResourceLocation(location.getResourceDomain(), path);
    }

    public static void loadOverrides(List<File> skip) throws Exception
    {
        File modFolder = new File(Loader.instance().getConfigDir(), Helper.MODID);
        if (!modFolder.exists())
        {
            modFolder.mkdirs();
        }
        Helper.makeReadme(modFolder);
        File rootFolder = new File(modFolder, "overrides");
        if (!rootFolder.exists())
        {
            rootFolder.mkdirs();
        }
        Path root = rootFolder.toPath();
        List<File> list = Helper.findXMLFiles(rootFolder, new ArrayList<>());
        list.removeAll(skip);
        for (File f : list)
        {
            String path = root.relativize(f.toPath()).toString().replaceFirst("\\\\|/", ":");
            XmlParser.addOverrideXml(new ResourceLocation(path), f);
        }
    }

    public static String truncate(String text, int width)
    {
        return text.length() < width ? text : text.substring(0, width) + "...";
    }

    public static String itemstackToString(ItemStack stack)
    {
        if (stack == null)
        {
            return "null";
        }
        StringBuilder sb = new StringBuilder("<");
        sb.append(stack.getItem().getRegistryName());
        int meta = stack.getMetadata();
        if (meta != 0)
        {
            sb.append(meta);
        }
        sb.append('>');
        if (stack.getCount() != 1)
        {
            sb.append('*').append(stack.getCount());
        }
        return sb.toString();
    }

    public static double round(double in, double precision)
    {
        return precision * Math.floor(in / precision);
    }
}
