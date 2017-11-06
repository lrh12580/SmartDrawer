package com.isaac.smartdrawer;

/**
 * Created by Isaac on 2016/7/29 0029.
 */
public class ContentDbSchema {
    public static final class ContentsTable {
        public static final String NAME = "contents";

        public static final class Cols {
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String CATEGORY = "category";
            public static final String EXIST = "exist";
        }
    }

    public static final class CategoriesTable {
        public static final String NAME = "categories";

        public static final class Cols {
            public static final String CATEGORY = ContentsTable.Cols.CATEGORY;
        }
    }

    public static final class AlarmsTable {
        public static final String NAME = "alarms";

        public static final class Cols {
            public static final String ALARM_ID = "alarm_id";
            public static final String DATE = "date";
            public static final String IDS = "ids";
            public static final String INFO = "info";
        }
    }

    public static final class HistoryTable {
        public static final String NAME = "history";

        public static final class Cols {
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String CATEGORY = "category";
            public static final String EXIST = "exist";
            public static final String DATE = "date";
        }
    }
}
