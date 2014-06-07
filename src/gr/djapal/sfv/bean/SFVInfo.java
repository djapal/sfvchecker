/*
 *  SFV info bean
 *  Copyright (c) 2009 Apostolos Alexiadis <djapal@gmail.com>
 *                All Rights Reserved
 *
 *  This program is free software. It comes without any warranty, to
 *  the extent permitted by applicable law. You can redistribute it
 *  and/or modify it under the terms of the Do What the Fuck You Want
 *  to Public License, Version 2, as published by Sam Hocevar. See
 *  http://www.wtfpl.net/ for more details.
 */
package gr.djapal.sfv.bean;

import javafx.beans.property.SimpleStringProperty;

public class SFVInfo {

    private final SimpleStringProperty filename;
    private final SimpleStringProperty all;
    private final SimpleStringProperty good;
    private final SimpleStringProperty bad;
    private final SimpleStringProperty missing;

    
    public SFVInfo(String filename, String all, String good, String bad, String missing) {
        this.filename = new SimpleStringProperty(filename);
        this.all = new SimpleStringProperty(all);
        this.good = new SimpleStringProperty(good);
        this.bad = new SimpleStringProperty(bad);
        this.missing = new SimpleStringProperty(missing);
    }

    public String getFilename() {
        return filename.get();
    }

    public void setFilename(String fName) {
    	filename.set(fName);
    }

    public String getAll() {
        return all.get();
    }

    public void setAll(String allFiles) {
        all.set(allFiles);
    }

    public String getGood() {
        return good.get();
    }

    public void setGood(String goodOnes) {
    	good.set(goodOnes);
    }
    
    public String getBad() {
        return bad.get();
    }

    public void setBad(String badOnes) {
    	bad.set(badOnes);
    }
    
    public String getMissing() {
        return missing.get();
    }

    public void setMissing(String missingOnes) {
    	missing.set(missingOnes);
    }
    
    
    
    
    public SimpleStringProperty filenameProperty() {
        return filename;
    }

    public SimpleStringProperty allProperty() {
        return all;
    }

    public SimpleStringProperty goodProperty() {
        return good;
    }

    public SimpleStringProperty badProperty() {
        return bad;
    }

    public SimpleStringProperty missingProperty() {
        return missing;
    }
    
    
}
