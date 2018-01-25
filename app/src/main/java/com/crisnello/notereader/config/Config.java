package com.crisnello.notereader.config;

	public class Config {

        //SYSTEM CONSTANTS
        public static int WS_TIME_OUT = 15000;
        public final static String DB_NAME = "banco_de_notas.sqlite";

        //public final static String WS_SERVER_USADO = "http://172.17.6.56:8080";
        public final static String WS_SERVER_USADO = "http://177.71.246.197";

        public final static String WS_URL_LOGIN = WS_SERVER_USADO+"/bancodenotas/pages/home/api/login.jsf";
        public final static String WS_URL_NOTA = WS_SERVER_USADO+"/bancodenotas/pages/nota/api/notaAdd.jsf";
        public final static String WS_URL_NOTAS = WS_SERVER_USADO+"/bancodenotas/pages/nota/api/notas.jsf";
        public final static String WS_URL_ADD_USER = WS_SERVER_USADO+"/bancodenotas/pages/usuario/api/usuarioAdd.jsf";


        //public final static String WS_FILE_PATH="system/file/";

            public final static String ADMOB_APP_ID = "ca-app-pub-8704319073007954~8494480031";


    }

