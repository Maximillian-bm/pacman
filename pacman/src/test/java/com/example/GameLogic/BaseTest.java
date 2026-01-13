package com.example.GameLogic;

import org.junit.Rule;
import org.junit.rules.Timeout;

public abstract class BaseTest {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(1);
}
