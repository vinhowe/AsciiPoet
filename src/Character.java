public class Character {
  public int x;
  public int y;
  public char value;
  
  public Character(char value, int x, int y) {
    this.value = value;
    this.x = x;
    this.y = y;
  }
  
  public boolean samePosition(Character compareTo) {
    return x == compareTo.x && y == compareTo.y;
  }

  public boolean isAtPosition(int x, int y) {
    return this.x == x && this.y == y;
  }
  
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Character)) {
      return false;
    }
    Character compareTo = (Character) other;
    return x == compareTo.x && y == compareTo.y && value == compareTo.value;
  }
}