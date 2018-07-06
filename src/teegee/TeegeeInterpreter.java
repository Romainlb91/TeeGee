/* This file is part of sableccWiki.
 *
 * See the NOTICE file distributed with this work for copyright information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teegee;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import teegee.language_teegee.*;
import teegee.walker.*;
import teegee.macro.*;

public class TeegeeInterpreter {

    public static Map<String,List<String>> allFile = new HashMap<>();
    public static List<Reader> readFile = new LinkedList<>();
    public static List<String> linkFile = new LinkedList<>();
    public static List<MNavBar> allNavBar = new LinkedList<>();
    public static List<MDropDownNavBar> allDownNavBar = new LinkedList<>();

    public static void main(
            String[] args) {

        String siteName = "";
        String absolutePathSiteName = "";
        if (args.length == 0) {
            System.err.println("INPUT ERROR: argument not found .");
            System.exit(1);
        }
        else if (args.length == 1) {
            File rep = new File(args[0]);
            siteName = rep.getName().toUpperCase();
            new File(siteName+"-HTML").mkdirs();
            absolutePathSiteName = new File(siteName+"-HTML").getAbsolutePath();
            allFile.put(rep.getName(),new LinkedList<>());
            allFile = folderSearch(rep,allFile,siteName);
            createNavBar(allFile,args[0], absolutePathSiteName);
            copyCss(siteName);
            copyImages(siteName);
            System.out.println("TeeGee, a text generator");
            System.out.println("by Romain Le Breton <romain.lebreton91@gmail.com> and other contributors.");
            System.out.println("Compiling \"" + args[0] + "\"");
        }
        else {
            System.err.println("COMMAND-LINE ERROR: too many arguments.");
            System.exit(1);
        }
        for (int nbFile = 0; nbFile < readFile.size(); nbFile++) {
            Node syntaxTree = null;

            try {
                // parse
                syntaxTree = new Parser(readFile.get(nbFile)).parse();
            } catch (IOException e) {
                String inputName;
                if (args.length == 0) {
                    inputName = "standard input";
                } else {
                    inputName = "file '" + args[0] + "'";
                }
                System.err.println("INPUT ERROR: " + e.getMessage()
                        + " while reading " + inputName + ".");
                System.exit(1);
            } catch (ParserException e) {
                System.err.println("SYNTAX ERROR: " + e.getMessage() + ".");
                System.exit(1);
            } catch (LexerException e) {
                System.err.println("LEXICAL ERROR: " + e.getMessage() + ".");
                System.exit(1);
            }
            InterpreterEngine interpreterEngine = new InterpreterEngine();
            interpreterEngine.createHtml(siteName, allFile, linkFile.get(nbFile), allNavBar, allDownNavBar, absolutePathSiteName);
            interpreterEngine.visit(syntaxTree);
            interpreterEngine.showPage(siteName+"-HTML");
            //TreeVisualizer treeVisualizer = new TreeVisualizer();
            //treeVisualizer.printTree(syntaxTree);
        }
        System.out.println("Done compiling \"" + args[0] + "\"");
        // finish normally
        System.exit(0);
    }

    public static Map<String,List<String>> folderSearch(File rep, Map<String,List<String>> map, String siteName) {
        File [] allFiles = rep.listFiles();
        for(int i = 0; i < allFiles.length; ++i) {
            File actualFile = allFiles[i];
            if (actualFile.isDirectory()) {
                new File(siteName+"-HTML/"+ actualFile.getPath().substring(actualFile.getPath().indexOf("/") +1 )).mkdirs();
                map.get(rep.getPath()).add(actualFile.getName());
                map.put(actualFile.getPath(), new LinkedList<>());
                map.putAll(folderSearch(actualFile,map,siteName));
            } else if(actualFile.getName().endsWith(".teegee")){
                try{
                    readFile.add(new FileReader(actualFile));
                    linkFile.add(actualFile.getPath());
                }catch (FileNotFoundException e) {
                    System.err.println("INPUT ERROR: file not found '" + actualFile.getName()
                            + "'.");
                    System.exit(1);
                }
                List<String> test = map.get(rep.getPath());
                if(test != null) {
                    map.get(rep.getPath()).add(actualFile.getName());
                }else{
                    List<String> list = new LinkedList<>();
                    list.add(actualFile.getName());
                    map.put(rep.getPath(), list);
                }
            }
        }
        return  map;
    }

    public static void createNavBar(Map<String,List<String>> map, String siteName, String absolutePathSiteName){
        List<String> value = map.get(siteName);
        for (int i=0; i<value.size(); i++) {
            if (value.get(i).endsWith(".teegee")){
                String name = value.get(i).substring(0, value.get(i).indexOf(".teegee"));
                String destination = absolutePathSiteName + "/" + name;
                MNavBar navBar = new MNavBar(name,destination);
                allNavBar.add(navBar);
            }
            else {
                String name = value.get(i);
                MDropDownNavBar DropDownNavBar = new MDropDownNavBar(name);
                DropDownNavBar = createDropDownNavBar(map, siteName + "/"+ name, absolutePathSiteName + "/" + name, DropDownNavBar);
                allDownNavBar.add(DropDownNavBar);
            }
        }
    }
    //This function is to add files in a dropDownNavBar, recursively.
    public static MDropDownNavBar createDropDownNavBar(Map<String,List<String>> map, String folder, String absolutePathFolder, MDropDownNavBar DropDownNavBar){
        List<String> value = map.get(folder);
        for (int i=0; i<value.size(); i++) {
            if (value.get(i).endsWith(".teegee")){
                String name = value.get(i).substring(0, value.get(i).indexOf(".teegee"));
                String destination = absolutePathFolder + "/" + name;
                MDownNavBar navBar = new MDownNavBar(name,destination);
                DropDownNavBar.addDownNavBar(navBar);
            }
            else {
                String name = value.get(i);
                DropDownNavBar = createDropDownNavBar(map, folder + "/" +name, absolutePathFolder + "/" + name, DropDownNavBar);
            }
        }
        return DropDownNavBar;
    }



    public static void copyCss (String siteName){
        new File(siteName + "-HTML/css/").mkdirs();
        File source_css = new File("css/styles.css");
        Path dest_css = Paths.get(siteName + "-HTML/css/styles.css");
        try {

            Files.copy(source_css.toPath(), dest_css, StandardCopyOption.REPLACE_EXISTING);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void copyImages (String siteName) {
        new File(siteName + "-HTML/images/").mkdirs();
        File source_img = new File("images/menu-icon.png");
        Path dest_img = Paths.get(siteName + "-HTML/images/menu-icon.png");
        try {

            Files.copy(source_img.toPath(), dest_img, StandardCopyOption.REPLACE_EXISTING);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
