package comum.progressbar;

/**
 * Represents the display style of a progress bar.
 *
 * @author Tongfei Chen
 * @since 0.5.1
 */
public enum ProgressBarStyle {

    COLORFUL_UNICODE_BLOCK("\u001b[33m│", "│\u001b[0m", '█', ' ', " ▏▎▍▌▋▊▉"),

    /**
     * Use Unicode block characters to draw the progress bar.
     */
    UNICODE_BLOCK("│", "│", '█', ' ', " ▏▎▍▌▋▊▉"),

    /**
     * Use only ASCII characters to draw the progress bar.
     */
    ASCII("[", "]", '=', ' ', ">");

    String leftBracket;
    String rightBracket;
    char block;
    char space;
    String fractionSymbols;

    ProgressBarStyle(String leftBracket, String rightBracket, char block, char space, String fractionSymbols) {
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.block = block;
        this.space = space;
        this.fractionSymbols = fractionSymbols;
    }

}
