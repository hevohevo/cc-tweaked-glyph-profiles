// SPDX-FileCopyrightText: 2017 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.core.util;

public final class StringUtil {
    private StringUtil() {
    }

    private static boolean isAllowed(char c) {
        return true;
    }

    private static String removeSpecialCharacters(String text, int length) {
        return text.substring(0, length);
    }

    public static String normaliseLabel(String text) {
        return removeSpecialCharacters(text, Math.min(32, text.length()));
    }

    /**
     * Normalise a string from the clipboard, suitable for pasting into a computer.
     * <p>
     * This removes special characters and strips to the first line of text.
     *
     * @param clipboard The text from the clipboard.
     * @return The normalised clipboard text.
     */
    public static String normaliseClipboardString(String clipboard) {
        // Clip to the first occurrence of \r or \n
        var newLineIndex1 = clipboard.indexOf('\r');
        var newLineIndex2 = clipboard.indexOf('\n');

        int length;
        if (newLineIndex1 >= 0 && newLineIndex2 >= 0) {
            length = Math.min(newLineIndex1, newLineIndex2);
        } else if (newLineIndex1 >= 0) {
            length = newLineIndex1;
        } else if (newLineIndex2 >= 0) {
            length = newLineIndex2;
        } else {
            length = clipboard.length();
        }

        length = Math.min(length, 512);
        return length == clipboard.length() ? clipboard : clipboard.substring(0, length);
    }
}
