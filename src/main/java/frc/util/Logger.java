/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Add your docs here.
 */
public class Logger {

    public static final String LOG_FILE_DIR = "/home/lvuser/logs/";
    private ArrayList<Buffer> logStreams = new ArrayList<>();

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM_dd_yyyy");
    public static LocalDate now = LocalDate.now();

    class Buffer {
        public static final int BUFFER_SIZE = 1024;
        Object[] buf = new Object[BUFFER_SIZE];
        String name;
        String filename;
        int size = 0;

        public Buffer(String name) {
            this.name = name;
            filename = "log" + "_" + name + "_" + dtf.format(now);
        }

        private boolean add(Object obj) {
            buf[size] = obj;
            size++;
            if (size == BUFFER_SIZE) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void createLogStream(String name) {
        logStreams.add(new Buffer(name));
    }

    private Buffer findBuffer(String name) {
        for (Buffer i : logStreams) {
            if (i.name.equals(name)) {
                return i;
            }
        }
        return null;
    }

    public void logDoubles(String name, double... doubles) {
        findBuffer(name).add(doubles);

        if (findBuffer(name).add(doubles) == true) {
            flushBuffer(findBuffer(name));
        }
    }

    public void logEvent(String name, String string) {
        findBuffer(name).add(string);

        if (findBuffer(name).add(string) == true) {
            flushBuffer(findBuffer(name));
        }
    }

    public void flush(String name) {
        try {
            flushBuffer(findBuffer(name));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void flushBuffer(Buffer buf) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(LOG_FILE_DIR + buf.filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            for(int i=0; i<buf.size; i++){
                if(buf.buf[i] instanceof double[]){
                    double[] bufDouble = (double[]) buf.buf[i];
                    for(int j = 0; j < bufDouble.length - 1; j++){
                        fw.write(Double.toString(bufDouble[j]));
                        fw.write(", ");
                    }
                    fw.write(Double.toString(bufDouble[bufDouble.length - 1]));
                }else if (buf.buf[i] instanceof String) {
                    fw.write((String) buf.buf[i]);
                }
                fw.write("\n");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf.size = 0;
    }
}
