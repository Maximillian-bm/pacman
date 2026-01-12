package com.example.GameLogic;

public class test {
    public static void main(String[] arg){
        String local = "tcp://127.0.0.1:50000/?keep";
        String remote = "tcp://pacman.maximillian.info:50000/?keep";

        System.out.println(URIUtil.getSpace1URI(local));
        System.out.println(URIUtil.getSyncURI(local, 0));
        System.out.println(URIUtil.getRawActionURI(local, 0));
        System.out.println(URIUtil.getCleanActionURI(local, 0));
        System.out.println(URIUtil.getLobbyID(URIUtil.getSyncURI(local, 0)));

        System.out.println(URIUtil.getSpace1URI(remote));
        System.out.println(URIUtil.getSyncURI(remote, 0));
        System.out.println(URIUtil.getRawActionURI(remote, 0));
        System.out.println(URIUtil.getCleanActionURI(remote, 0));
        System.out.println(URIUtil.getLobbyID(URIUtil.getSyncURI(remote, 0)));
    }
}
