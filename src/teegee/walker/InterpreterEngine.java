/* This file is part of Mino.
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

package teegee.walker;

import teegee.exception.*;
import teegee.language_teegee.*;
import teegee.macro.*;
import java.io.*;
import java.util.*;

public class InterpreterEngine
        extends Walker {

    private String NamePage = "";
    private String PathPage = "";
    private String expString = "";
    private String expArray = "";
    private MHtml mHtml = null;
    private MBulletedList bulleted_list = new MBulletedList();
    private MNumberList number_list = new MNumberList();
    private MTable array = new MTable();
    private MRow row = new MRow();
    private List<MColumn> column = new LinkedList<>();
    private MLink stringLink = new MLink("","");
    private MLink ampersandLink = new MLink("","");

    //private MColumn column = null;

    public void visit(
            Node node) {

        node.apply(this);
    }

    public void createHtml(
            String site_name, Map<String,List<String>> allFile, String link,List<MNavBar> allNavBar,List<MDropDownNavBar> allDropDownNavBar, String parentFloder) {
        this.mHtml = new MHtml(site_name, parentFloder + "/");
        this.NamePage = link.substring(link.lastIndexOf("/") +1, link.indexOf(".teegee")); ;
        this.PathPage = link.substring(link.indexOf("/"), link.indexOf(".teegee"));
        MPage Page_Name = new MPage(NamePage);
        this.mHtml.addPageName(Page_Name);
        //Insert all element in navbar
        for(int navBar=0; navBar<allNavBar.size(); navBar++){
            this.mHtml.addNavBar(allNavBar.get(navBar));
        }
        for(int dropDownNavBar=0; dropDownNavBar<allDropDownNavBar.size(); dropDownNavBar++){
            this.mHtml.addNavBar(allDropDownNavBar.get(dropDownNavBar));
        }

    }

    public void showPage(String link){

        Writer writer = null;
        String file = link + PathPage + ".html";
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "utf-8"));
            writer.write(mHtml.build());
        } catch (IOException ex) {
            // Report
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }

    @Override
    public void caseMarker_Title(
            NMarker_Title node) {
        String value = getString(node.get_String());
        int number = Integer.parseInt(node.get_Num().getText());
        if(number < 1 || number > 6 ){
            throw new InterpreterException("The number must be between 1 and 6",
                    node.get_Num());
        }
        MTitre titre = new MTitre(value, String.valueOf(number));
        mHtml.addParts(titre);
    }

    @Override
    public void caseMarker_TitleWithBulletedList (
            NMarker_TitleWithBulletedList node) {
        setList(node.get_BulletedList().get_BulletedListItems());
        mHtml.addParts(bulleted_list);
        bulleted_list = new MBulletedList();
        String value = getString(node.get_StringOrLink());
        int number = Integer.parseInt(node.get_Num().getText());
        if(number < 1 || number > 6 ){
            throw new InterpreterException("The number must be between 1 and 6",
                    node.get_Num());
        }
        MTitre titre = new MTitre(value, String.valueOf(number));
        mHtml.addParts(titre);
    }

    @Override
    public void caseMarker_TitleWithNumberList (
            NMarker_TitleWithNumberList node) {
        setList(node.get_NumberList().get_NumberListItems());
        mHtml.addParts(number_list);
        number_list = new MNumberList();
        String value = getString(node.get_StringOrLink());
        int number = Integer.parseInt(node.get_Num().getText());
        if(number < 1 || number > 6 ){
            throw new InterpreterException("The number must be between 1 and 6",
                    node.get_Num());
        }
        MTitre titre = new MTitre(value, String.valueOf(number));
        mHtml.addParts(titre);
    }

    @Override
    public void caseMarker_Paragraph(
            NMarker_Paragraph node) {
        String value = getString(node.get_AmpersandStrings());
        MParagraph Paragraph = new MParagraph();
        MText text = new MText(value);
        Paragraph.addParts(text);
        mHtml.addParts(Paragraph);
    }

    @Override
    public void caseMarker_ParagraphWithBulletedList(
            NMarker_ParagraphWithBulletedList node) {
        setList(node.get_BulletedList().get_BulletedListItems());
        mHtml.addParts(bulleted_list);
        bulleted_list = new MBulletedList();
        String value = getString(node.get_AmpersandStrings());
        MParagraph Paragraph = new MParagraph();
        MText text = new MText(value);
        Paragraph.addParts(text);
        mHtml.addParts(Paragraph);
    }

    @Override
    public void caseMarker_ParagraphWithNumberList(
            NMarker_ParagraphWithNumberList node) {
        setList(node.get_NumberList().get_NumberListItems());
        mHtml.addParts(number_list);
        number_list = new MNumberList();
        String value = getString(node.get_AmpersandStrings());
        MParagraph Paragraph = new MParagraph();
        MText text = new MText(value);
        Paragraph.addParts(text);
        mHtml.addParts(Paragraph);
    }

    @Override
    public void caseMarker_String(
            NMarker_String node) {
        String value = getString(node.get_StringOrLink());
        MText string = new MText(value);
        string.addLink(this.stringLink);
        mHtml.addParts(string);
    }

    @Override
    public void caseMarker_StringWithBulletedList(
            NMarker_StringWithBulletedList node) {
        setList(node.get_BulletedList().get_BulletedListItems());
        mHtml.addParts(bulleted_list);
        bulleted_list = new MBulletedList();
        String value = getString(node.get_StringOrLink());
        MText string = new MText(value);
        string.addLink(this.stringLink);
        mHtml.addParts(string);
    }

    @Override
    public void caseMarker_StringWithNumberList(
            NMarker_StringWithNumberList node) {
        setList(node.get_NumberList().get_NumberListItems());
        mHtml.addParts(number_list);
        number_list = new MNumberList();
        String value = getString(node.get_StringOrLink());
        MText string = new MText(value);
        string.addLink(this.stringLink);
        mHtml.addParts(string);
    }


    @Override
    public void caseMarker_Eol(
            NMarker_Eol node) {
        MEndOfLine Eol = new MEndOfLine();
        mHtml.addParts(Eol);
    }

    @Override
    public void caseMarker_EolWithBulletedList (
            NMarker_EolWithBulletedList node) {
        setList(node.get_BulletedList().get_BulletedListItems());
        mHtml.addParts(bulleted_list);
        bulleted_list = new MBulletedList();
        MEndOfLine Eol = new MEndOfLine();
        mHtml.addParts(Eol);
    }

    @Override
    public void caseMarker_EolWithNumberList (
            NMarker_EolWithNumberList node) {
        setList(node.get_NumberList().get_NumberListItems());
        mHtml.addParts(number_list);
        number_list = new MNumberList();
        MEndOfLine Eol = new MEndOfLine();
        mHtml.addParts(Eol);
    }

    private void setList(
            Node node) {
        visit(node);
    }

    @Override
    public void caseBulletedListItem (
            NBulletedListItem node) {
        MList mList = new MList(getString(node.get_String()));
        bulleted_list.addContent(mList);
    }

    @Override
    public void caseNumberListItem (
            NNumberListItem node) {
        MList mList = new MList(getString(node.get_String()));
        number_list.addContent(mList);
    }

    @Override
    public void caseMarker_Array(
            NMarker_Array node) {
        MTable value = getArray(node.get_Array());
        mHtml.addParts(value);
    }

    @Override
    public void caseMarker_ArrayWithBulletedList (
            NMarker_ArrayWithBulletedList node) {
        setList(node.get_BulletedList().get_BulletedListItems());
        mHtml.addParts(bulleted_list);
        bulleted_list = new MBulletedList();
        MTable value = getArray(node.get_Array());
        mHtml.addParts(value);
    }

    @Override
    public void caseMarker_ArrayWithNumberList(
            NMarker_ArrayWithNumberList node) {
        setList(node.get_NumberList().get_NumberListItems());
        mHtml.addParts(number_list);
        number_list = new MNumberList();
        MTable value = getArray(node.get_Array());
        mHtml.addParts(value);
    }

    private MTable getArray(
            Node node) {
        visit(node);
        return array;
    }

    @Override
    public void caseArray (
            NArray node) {
        visit(node.get_ArrayRows());
        visit(node.get_ColumnChar());
    }

    @Override
    public void caseColumnChar_Pipe (
            NColumnChar_Pipe node) {
        MColumn newColumn = new MColumn(this.expArray);
        this.column.add(newColumn);
        this.expArray = "";
        for(int i=0; i<this.column.size(); i++){
            this.row.addContent(this.column.get(i));
        }
        array.addContent(this.row);
        this.row = new MRow();
        this.column = new LinkedList<>();
    }

    @Override
    public void caseColumnChar_Char (
            NColumnChar_Char node) {
        this.expArray += node.get_Char().getText();

    }
    @Override
    public void caseColumnChar_Num (
            NColumnChar_Num node) {
        this.expArray += node.get_Num().getText();

    }
    @Override
    public void caseColumnChar_Eol (
            NColumnChar_Eol node) {
        this.expArray += "<br/>";

    }
    @Override
    public void caseColumnChar_Ampersand (
            NColumnChar_Ampersand node) {
        MColumn newColumn = new MColumn(this.expArray);
        this.column.add(newColumn);
        this.expArray = "";
    }


    @Override
    public void caseStringOrLink_Link (
            NStringOrLink_Link node) {
        this.stringLink = new MLink(getString(node.get_String()),getString(node.get_AmpersandStrings()));
    }

    private String getString(
            Node node) {
        visit(node);
        String expString = this.expString;
        this.expString = "";
        return expString;
    }

    @Override
    public void caseStringChar_Char(
            NStringChar_Char node) {

        this.expString += node.get_Char().getText();
    }

    @Override
    public void caseStringChar_Num(
            NStringChar_Num node) {

        this.expString += node.get_Num().getText();
    }

    @Override
    public void caseStringChar_Sharp(
            NStringChar_Sharp node) {

        this.expString += "#";
    }

    @Override
    public void caseStringChar_Ampersand(
            NStringChar_Ampersand node) {

        this.expString += "&";
    }

    @Override
    public void caseStringChar_Star(
            NStringChar_Star node) {

        this.expString += "*";
    }

    @Override
    public void caseStringChar_Circumflex(
            NStringChar_Circumflex node) {

        this.expString += "^";
    }

    @Override
    public void caseStringChar_Pipe(
            NStringChar_Pipe node) {

        this.expString += "|";
    }

    @Override
    public void caseStringChar_Backslash(
            NStringChar_Backslash node) {

        this.expString += "\\";
    }

    @Override
    public void caseAmpersandChar_Char(
            NAmpersandChar_Char node) {

        this.expString += node.get_Char().getText();
    }

    @Override
    public void caseAmpersandChar_Num(
            NAmpersandChar_Num node) {

        this.expString += node.get_Num().getText();
    }
    @Override
    public void caseAmpersandChar_Eol (
            NAmpersandChar_Eol node) {

        this.expString += node.get_Eol().getText();
    }

    @Override
    public void caseAmpersandChar_Sharp(
            NAmpersandChar_Sharp node) {

        this.expString += "#";
    }

    @Override
    public void caseAmpersandChar_Ampersand(
            NAmpersandChar_Ampersand node) {

        this.expString += "&";
    }

    @Override
    public void caseAmpersandChar_Star(
            NAmpersandChar_Star node) {

        this.expString += "*";
    }

    @Override
    public void caseAmpersandChar_Circumflex(
            NAmpersandChar_Circumflex node) {

        this.expString += "^";
    }

    @Override
    public void caseAmpersandChar_Backslash(
            NAmpersandChar_Backslash node) {

        this.expString += "\\";
    }
    @Override
    public void caseAmpersandChar_Pipe(
            NAmpersandChar_Pipe node) {

        this.expString += "|";
    }

}
