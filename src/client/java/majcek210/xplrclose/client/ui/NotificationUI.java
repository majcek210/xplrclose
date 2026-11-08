package majcek210.xplrclose.client.ui;

public class NotificationUI {
    public static final int WIDTH = 160, HEIGHT = 30, GAP = 4, MARGIN = 6;
    public static final int CLOSE = 12, IGN_W = 44, IGN_H = 12;

    public record Rectangle(int x, int y, int w, int h) {
        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >=y && my < y +h;
        }
    }

    public static Rectangle box(int index, int screenW, int screenH) {
        int x = screenW - WIDTH - MARGIN;
        int y = screenH - MARGIN - (index + 1) * HEIGHT - index * GAP;
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }


    public static Rectangle closeButton(Rectangle b) {
        return new Rectangle(b.x() + b.w() - CLOSE - 4, b.y() + 4, CLOSE, CLOSE);
    }
    public static Rectangle ignoreButton(Rectangle b) {
        return new Rectangle(b.x() + b.w() - IGN_W - 4, b.y() + b.h() - IGN_H - 3,IGN_W, IGN_H);
    }
}
