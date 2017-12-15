package fr.univtln.cniobechoudayer.pimpmytrip.database;

public class DBConstants {

    // The database name
    public static final String DATABASE_NAME = "InternalSQLite.db";

    // The database version
    public static final int DATABASE_VERSION = 1;

    // The table Name
    public static final String MY_TABLE = "user_information";

    // ## Column name ##
    public static final String KEY_COL_ID = "_id";// Mandatory

    public static final String KEY_COL_NUMERO = "numero";

    public static final String KEY_COL_TEXT = "text";

    // Indexes des colonnes
    // The index of the column ID
    public static final int ID_COLUMN = 0;

    // The index of the column NUMERO
    public static final int NUMERO_COLUMN = 1;

    // The index of the column TEXT
    public static final int TEXT_COLUMN = 2;

} 
