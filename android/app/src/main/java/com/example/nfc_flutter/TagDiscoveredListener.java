package com.example.nfc_flutter;

import android.nfc.Tag;

public interface TagDiscoveredListener {
    void onTagDiscovered(Tag tag);
}
