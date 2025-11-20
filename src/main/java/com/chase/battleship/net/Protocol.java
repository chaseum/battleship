package com.chase.battleship.net;

import com.chase.battleship.core.*;

public final class Protocol {
        private Protocol() {}

        public static String formatAction(TurnAction action) {
                return NetUtil.encodeAction(action);
        }

        public static TurnAction parseClientCommand(String line) {
                return NetUtil.decodeAction(line);
        }
}