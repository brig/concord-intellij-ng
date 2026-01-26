import React from "react";

type OS = "mac" | "win";

function detectOS(): OS {
    if (typeof window !== "undefined") {
        const p = new URLSearchParams(window.location.search).get("os");
        if (p === "mac" || p === "win") return p;

        const plat = navigator.platform?.toLowerCase() ?? "";
        const ua = navigator.userAgent?.toLowerCase() ?? "";
        const isMac = plat.includes("mac") || ua.includes("mac os");
        return isMac ? "mac" : "win";
    }
    return "win";
}

export type KeyToken = { icon?: string; label: string };

function Keys({ keys }: { keys: KeyToken[] }) {
    return (
        <span className="keystroke__keystrokes">
      {keys.map((k, idx) => (
          <kbd className="keystroke" key={idx}>
              {k.icon ? <span className="keystroke__icon">{k.icon}</span> : null}
              {k.label}
          </kbd>
      ))}
    </span>
    );
}

export default function Keystroke({
                                      mac,
                                      win,
                                  }: {
    mac: KeyToken[];
    win: KeyToken[];
}) {
    const [os, setOs] = React.useState<OS>("win");

    React.useEffect(() => {
        setOs(detectOS());
    }, []);

    return os === "mac" ? <Keys keys={mac} /> : <Keys keys={win} />;
}
