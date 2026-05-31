package dev.jwall64.databridge.impl;

import java.util.PriorityQueue;
import java.util.Queue;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.timeline.Timeline;

public class TimelineTracker {

    MinecraftServer server;
    PriorityQueue<TimelineEvent> events;
    Timeline timeline;

    long lastTick = -1;

    public void init(MinecraftServer server, Timeline timeline) {
        this.server = server;
        this.timeline = timeline;

        this.events = ((TimelineExtension) timeline).getEvents().stream()
            .filter(event -> event.tick().isPresent())
            .collect(() -> new PriorityQueue<>(), Queue::add, Queue::addAll);
    }

    public void tick() {

        long ticks = timeline.getCurrentTicks(server.clockManager());
        if(ticks < lastTick) {
            // Time went backwards, reset
            events.clear();
            events.addAll(((TimelineExtension) timeline).getEvents().stream()
                .filter(event -> event.tick().isPresent() && event.tick().get() >= ticks)
                .toList());
        }
        lastTick = ticks;

        while(events.peek() != null && events.peek().tick().get() <= ticks) {
            events.poll().execute(server, ticks);
        }
    }

}
