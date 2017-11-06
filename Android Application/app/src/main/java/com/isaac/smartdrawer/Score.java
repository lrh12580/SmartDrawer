package com.isaac.smartdrawer;

import android.content.Context;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Score {
    private static String ans = "";
    private static int dd_ = 0, mm_ = 0, hh_ = 0, minute_ = 0;
    private static int dd = 0, mm = 0, hh = 0, minute = 0;
    private static int[] rating = new int[6];
    private static String op, str;
    private static Set<Item> set = new HashSet<Item>();
    public static final int WEEKMIN = 3;
    private Context mContext;
    private static FileService mFileService;
    private static String output = "{\"score\":{";
    private static String list;

    public Score(Context context, String list) {
        mContext = context;
        mFileService = new FileService(context);
        this.list = list;
    }

    public static void init() {
        int i;
        for (i = 1; i <= 4; i++) {
            rating[i] = 100;
        }
    }

    public static void analyse(int x) throws IOException
    {
        ans += "\"第" + x + "周\":{";
        int i, sum = 0, day_in, day_out;
        double res_in = 0, res_out = 0, variance_in = 0, variance_out = 0;
        Iterator<Item> it = set.iterator();

        while (it.hasNext()) {
            Item tmp = it.next();
            System.out.println(tmp.getName());
            sum = 0;
            day_in = 0; day_out = 0;
            res_in = 0; res_out = 0;
            variance_in = 0; variance_out = 0;

            for(i = 7 * x - 6; i <= 7 * x; i++)
            {
                sum += tmp.day[i].getCnt_out();
                if(tmp.day[i].getCnt_out() != 0)
                {
                    day_out ++;
                    res_out += tmp.day[i].getOutTime(1);
                }

                if(tmp.day[i].getCnt_in() != 0)
                {
                    day_in ++;
                    res_in += tmp.day[i].getInTime(tmp.day[i].getCnt_in());
                }

            }

            if(day_out > 0) {
                res_out /= day_out;
                tmp.setOutTime(res_out);
            } if(day_in > 0) {
                res_in /= day_in;
                if(tmp.getInTime() != 0)
                {
                    tmp.setInTime((tmp.getInTime() + res_in)/2);
                } else {
                    tmp.setInTime(res_in);
                }
            }

            for(i = 7 * x - 6; i <= 7 * x; i++)
            {
                if(!tmp.day[i].isExist()) {
                    rating[(i + 6)/7] -= (int)((100.0/7)/(set.size()));
                }
                if(tmp.day[i].getCnt_out() != 0)
                {
                    variance_out += (res_out - tmp.day[i].getOutTime(1))*(res_out - tmp.day[i].getOutTime(1));
                }
                if(tmp.day[i].getCnt_in() != 0)
                {
                    int t = tmp.day[i].getInTime(tmp.day[i].getCnt_in());
                    variance_out += (res_in - t) * (res_in - t);
                }
            }

            if(day_out > 0)
                variance_out = Math.sqrt(variance_out/day_out);
            if(day_in > 0)
                variance_in = Math.sqrt(variance_in/day_in);

            ans +=  "\""+tmp.getName() + "\":{";
            ans += "\"日均使用\":" + sum / 7.0 + ",";

            if(sum <= WEEKMIN)
            {
                ans += "\"使用频率\":\"较低\",";
                ans += ("\"第一次取出的平均时间\":\"" + ((sum == 0) ?"":((int)(res_out)/60 + ":" +(int)((res_out) - (res_out/60 * 60)))) + "\",");
                ans += ("\"第一次取出的时间的方差\":" + -1 + ",");
                ans += ("\"最后一次放回的平均时间\":\"" + ((sum == 0) ?"":((int)(res_in)/60 + ":" +(int)((res_in) - (res_in/60 * 60)))) + "\",");
                ans += ("\"最后一次放回的时间的方差\":" + -1 + "},");
            } else {
                if(sum <= 7){
                    ans += "\"使用频率\":\"适中\",";
                } else {
                    ans += "\"使用频率\":\"较高\",";
                }
                ans += ("\"第一次取出的平均时间\":\"" + (int)(res_out)/60 + ":" +(int)((res_out) - (res_out/60 * 60)) + "\",");
                ans += ("\"第一次取出的时间的方差\":" + variance_out + ",");
                ans += ("\"最后一次放回的平均时间\":\"" + (int)(res_in)/60 + ":" +(int)((res_in) - (res_in/60 * 60)) + "\",");
                ans += ("\"最后一次放回的时间的方差\":" + variance_in + "},");
            }

        }
        if(sum == 0) rating[x] = -1;
        ans += "\"分数\":"+(rating[x]!=-1?rating[x]:"-1")+"},\n";
        output += ans;
        ans = "";
    }

    public static void exe() throws IOException {
        init();
        Scanner cin = new Scanner(list);
        while (cin.hasNextLine()) {
            String text = cin.nextLine();
            @SuppressWarnings("resource")
            Scanner scan = new Scanner(text);
            op = scan.next();
            if (op.equals("end")) {
                int m = scan.nextInt();
                analyse(m);
                if (m == 4) break;
                continue;
            }
            Item item = new Item();
            str = scan.next();
            item.setName(str);
            if (op.equals("delete")) {
                set.remove(item);
                continue;
            }

            if (op.equals("add")) {
                set.add(item);
                continue;
            }

            dd = scan.nextInt();
            hh = scan.nextInt();
            minute = scan.nextInt();

            dd_ = dd;
            hh_ = hh;
            minute_ = minute;

            if (op.equals("in")) {
                Iterator<Item> it = set.iterator();
                while (it.hasNext()) {
                    Item tmp = it.next();
                    if (tmp.getName().equals(str)) {
                        tmp.day[dd].in(hh, minute);
                        tmp.day[dd].setExist(true);
                        break;
                    }
                }
            } else {
                Iterator<Item> it = set.iterator();
                while (it.hasNext()) {
                    Item tmp = it.next();
                    if (tmp.getName().equals(str)) {
                        tmp.day[dd].out(hh, minute);
                        tmp.day[dd].setExist(false);
                        if (hh * 60 + minute - tmp.getOutTime() >= 120 && tmp.getOutTime() != 0 && ((hh * 60 + minute) == tmp.day[dd].getOutTime(1))) {
                            rating[(dd + 6) / 7] -= 2;
//                            output += "在第" + dd + "天" + hh + "小时" + minute + "分钟";
//                            output += "取出时间较晚";
                        }
                        break;
                    }
                }
            }

        }

        int cnt = 0;
        for (int i = 1; i <= 4; i++) {
            if (rating[i] != -1) {
                rating[5] += rating[i];
                cnt++;
            }
        }

        if (cnt == 0) output += "\"该月的得分为\"：-1}}";
        else output += "\"该月的得分为\":" + rating[5]/cnt + "}}";
        mFileService.FileSave("output", output);
    }

}
