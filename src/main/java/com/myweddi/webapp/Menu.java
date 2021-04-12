package com.myweddi.webapp;

import java.util.Map;
import java.util.TreeMap;

public class Menu {
    public static final Map<String, String> adminMenu = new TreeMap<>();
    public static final Map<String, String> hostMenu = new TreeMap<>();
    public static final Map<String, String> guestMenu = new TreeMap<>();
    public static final Map<String, String> djMenu = new TreeMap<>();
    public static final Map<String, String> photojMenu = new TreeMap<>();
    static {
        adminMenu.put("home", "/admin/");

        guestMenu.put("home", "/home");
        guestMenu.put("Informacje", "/info");
        guestMenu.put("STOŁY", "/t");
        guestMenu.put("PREZENTY", "/");

        hostMenu.put("home", "/home");
        hostMenu.put("Informacje", "/host/info");
        hostMenu.put("Goście", "/host/guest");
        hostMenu.put("Stoły", "/host/tables");




    }
}
