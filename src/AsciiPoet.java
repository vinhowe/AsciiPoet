import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;

/**
 * Created by Thomas on 4/25/2017.
 */
public class AsciiPoet extends PApplet {

    private int cellHeight = 32;
    private int cellWidth = 20;
    private int rows;
    private int columns;
    private char[][] characters;
    private char drawingCharacter = '█';
    private ArrayList<Character> pendingCharacters;
    private boolean waitingForAltCode = false;
    private String altBuffer = "";
    private int[] selectStartPos = new int[2];
    private int[] selectCurrentPos = new int[2];
    private boolean isSelecting = false;
    private boolean showGrid = true;

    public static void main(String[] args) {
        PApplet.main("AsciiPoet", args);
    }

    @Override
    public void settings() {
        size(800, 800);
    }

    @Override
    public void setup() {
        rows = height/cellHeight;
        columns = width/cellWidth;

        characters = new char[height][width];

        pendingCharacters = new ArrayList();

        PFont font;
        font = createFont("Courier New", cellHeight);
        textFont(font);

        drawLine(5,5, 10, 2);
    }

    @Override
    public void draw() {
        int back = 0;
        background(back);

        fill(255);


        if(isSelecting) {
            int selectionStartX = selectStartPos[0]*cellWidth;
            int selectionStartY = selectStartPos[1]*cellHeight;

            int selectionBoxWidth = (selectCurrentPos[0]*cellWidth)-selectionStartX;
            int selectionBoxHeight = (selectCurrentPos[1]*cellHeight)-selectionStartY;

            rect(selectionStartX, selectionStartY, selectionBoxWidth, selectionBoxHeight);
        }

        stroke(64);

        if(showGrid) {
            for (int y = 0; y < rows; y++) {
                line(0,y*cellHeight,width,y*cellHeight);
            }

            for (int x = 0; x < columns; x++) {
                line(x*cellWidth,0,x*cellWidth,height);
            }
        }

        stroke(0,0,0,0);

        for (Character pending : pendingCharacters) {
            boolean invert = isSelecting && pending.x >= selectStartPos[0] && pending.y >= selectStartPos[1] && pending.x < selectCurrentPos[0] && pending.y <= selectCurrentPos[1];
            if(invert) {
                fill(0);
            }
            text(pending.value, pending.x*cellWidth, pending.y*cellHeight);
            if(invert) {
                fill(255);
            }
        }
    }


    public void drawLine(int x,int y,int x2, int y2) {
        int w = x2 - x;
        int h = y2 - y;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0) dx1 = -1;
        else if (w > 0) dx1 = 1;
        if (h < 0) dy1 = -1;
        else if (h > 0) dy1 = 1;
        if (w < 0) dx2 = -1;
        else if (w > 0) dx2 = 1;
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (!(longest > shortest)) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0) dy2 = -1;
            else if (h > 0) dy2 = 1;
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
            Character character = new Character(';', x, y);
            setCharacter(character, x, y);
            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
            }
        }
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) {
            mouseInput();
        } else if (mouseButton == RIGHT) {
            startSelection();
        } else {

        }
    }

    @Override
    public void mouseDragged() {
        if (mouseButton == LEFT) {
            mouseInput();
        } else if (mouseButton == RIGHT) {
            updateSelection();
        } else {

        }

    }

    @Override
    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            finishSelecting();
        }
    }

    void mouseInput() {
        touchCharacter(false);
    }

    void startSelection() {
        if(isSelecting) {
            return;
        }
        isSelecting = true;

        selectStartPos[0] = (mouseX/cellWidth);
        selectStartPos[1] = (mouseY/cellHeight);

        selectCurrentPos = selectStartPos.clone();
    }

    public void updateSelection() {
        selectCurrentPos[0] = Math.max((mouseX/cellWidth)+1, selectStartPos[0]+1);
        selectCurrentPos[1] = Math.max((mouseY/cellHeight)+1, selectStartPos[1]+1);

    }

    public void finishSelecting() {
        isSelecting = false;
        print("\n");
        String outputText = "";
        char[][] selectionBox = new char[Math.abs(selectCurrentPos[0]-selectStartPos[0])][Math.abs(selectCurrentPos[1]-selectStartPos[1])];
        for(int y = selectStartPos[1]+1; y <= selectCurrentPos[1]; y++) {
            for(int x = selectStartPos[0]; x < selectCurrentPos[0]; x++) {
                outputText += characters[x][y] == '\0' ? ' ' : characters[x][y];
            }
            outputText += '\n';
        }
        outputText = outputText.replace("\\", "\\\\");
        print(outputText);
    }

    private void touchCharacter(boolean toggle) {
        int cellX = (mouseX/cellWidth);
        int cellY = (mouseY/cellHeight)+1;

        cellX = Math.max(Math.min(cellX, columns), 0);
        cellY = Math.max(Math.min(cellY, rows), 0);

        Character newCharacter = new Character(drawingCharacter, cellX, cellY);

        for(int i = 0; i < pendingCharacters.size(); i++) {
            Character character = pendingCharacters.get(i);
            if(character.samePosition(newCharacter)) {
                if(!character.equals(newCharacter)) {
                    removeCharacter(cellX, cellY, i);
                    setCharacter(newCharacter, cellX, cellY);
                    return;
                }
                if(toggle) {
                    removeCharacter(cellX, cellY, i);
                }

                return;
            }
        }
        setCharacter(newCharacter, cellX, cellY);

    }

    public void removeCharacter(int x, int y, int i) {
        pendingCharacters.remove(i);
        characters[x][y] = 0;
    }

    public void setCharacter(Character character, int x, int y) {
        characters[x][y] = character.value;
        pendingCharacters.add(character);
    }

    @Override
    public void keyPressed() {
        if(waitingForAltCode) {


            if (key == CODED && keyCode == ENTER) {
                if (!altBuffer.equals("")) {
                    drawingCharacter = (char)(int)(Integer.valueOf(altBuffer));
                }
                altBuffer = "";
                waitingForAltCode = false;
            }
            String keyString = String.valueOf(key);

            try {
                altBuffer += keyString;
            } catch (NumberFormatException e) {
                if (!altBuffer.equals("")) {
                    drawingCharacter = (char)(int)(Integer.valueOf(altBuffer));
                    print("character is "+drawingCharacter+"\n");
                }
                altBuffer = "";
                waitingForAltCode = false;
            }
            return;
        }

        switch(key) {
            case 'q':
                showGrid = !showGrid;
                return;
            case 'w':
                pendingCharacters.clear();
                characters = new char[height][width];
                return;
        }

        int keyIndex = -1;
        if (key >= '!' && key <= '~') {
            keyIndex = key - 'A';
        } else if (key >= 128 && key <= 254) {
            keyIndex = key - 'a';
        }
        if (keyIndex == -1) {
            if(keyCode == CONTROL) {
                print("waiting for alt code\n");
                waitingForAltCode = true;
            }
            drawingCharacter = ' ';
        } else {
            drawingCharacter = key;
        }
    }
}
