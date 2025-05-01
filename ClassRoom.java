package multiThreading;

import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class WhiteBoard {
    String sentence;
    boolean producerFlag = true;
    static int globalConsumerIndex = 0;

    synchronized public void write(String sentence) {
//        Teacher entered into write()
//        Value of producerFlag is + producerFlag
        while (producerFlag == false) {
            try {
//        "Thread which is about to go to wait is " + Thread.currentThread().getName() + " with sentence " + "\"" + sentence + "\""
                wait();
//         "Thread which completed waiting " + Thread.currentThread().getName() + " with sentence " + "\"" + sentence + "\""
//         Value of producerFlag is + producerFlag
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        this.sentence = sentence;
        System.out.println("Teacher wrote " + "\"" + sentence + "\"");
        producerFlag = false;

        Student.initConsumerFlag();
        notifyAll();
    }

    synchronized public String read(String name) {
        while (producerFlag) {
            try {
                wait();
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }

//        Thread.currentThread().getName() +  entered into read()
        String text = sentence;

        if (text == null) {
            System.out.println(name + " is waiting for teacher to write.");
            return null;
        }

//        "Current thread is " + Thread.currentThread().getName() + " with sentence " + "\"" + text + "\""
        ++globalConsumerIndex;

        if (!text.toLowerCase().equals("end"))
            System.out.println(name + " copied " + "\"" + text + "\"");

        if (globalConsumerIndex == Student.getStudentCount()) {
            producerFlag = true;
            globalConsumerIndex = 0;
            notifyAll();
        }
//        Thread.currentThread().getName() + has completed read()

        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        return text;
    }
}

class Teacher extends Thread {

    private WhiteBoard board;
    private String[] sentenceSet;

    public Teacher(WhiteBoard board, String paragraph, String threadName) {
        sentenceSet = paragraph.split("\\.");
        this.board = board;
        setName(threadName);
    }

    public void run() {
//       Teacher started execution.
        for (String sentence : sentenceSet) {
            board.write(sentence);
        }
    }
}

class Student extends Thread {

    private static WhiteBoard board;
    private String name;
    private static int studentCount = 0;
    public int localConsumerIndex = 0;

    public Student(WhiteBoard v_board, String name, String threadName) {
        this.name = name;
        board = v_board;
        studentCount++;
        setName(threadName);
    }

    public static void initConsumerFlag() {
        board.consumerFlag = new boolean[studentCount];
        for (int i = 0; i < studentCount; i++) {
            board.consumerFlag[i] = false;
        }
    }

    public static int getStudentCount() {
        return studentCount;
    }

    public void run() {
//      name + started its execution.;
        String text = board.read(name);

        while (text == null || !text.toLowerCase().equals("end")) {
            text = board.read(name);
        }
    }
}

public class ClassRoom {

    public static void main(String[] args) {
        String paragraph = "I love my India."
                + "This is my India."
                + "My India is best."
                + "I am an Indian."
                + "end";

        WhiteBoard board = new WhiteBoard();
        Teacher t = new Teacher(board, paragraph, "Teacher1 thread");

        Student[] students = new Student[4];
        students[0] = new Student(board, "Jaya", "Jaya thread");
        students[1] = new Student(board, "Gyanesh", "Gyanesh thread");
        students[2] = new Student(board, "Jayesh", "Jayesh thread");
        students[3] = new Student(board, "Rishabh", "Rishabh thread");

        Student.initConsumerFlag();

        t.start();

        for (Student student : students) {
            student.start();
        }
    }
}
