import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Fairy extends Movable {

    public Fairy(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod, int health, int healthLimit) {
        super(id, position, images, animationPeriod, actionPeriod, health, healthLimit);
    }

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, this.createActivityAction(world, imageStore), super.getActionPeriod());
        scheduler.scheduleEvent(this, this.createAnimationAction(0), super.getAnimationPeriod());
    }

    public Action createAnimationAction(int repeatCount) {
        Animate animate = new Animate(this, repeatCount);
        return animate;
    }

    public Action createActivityAction(WorldModel world, ImageStore imageStore) {
        Activity activity = new Activity(this, world, imageStore);
        return activity;
    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fairyTarget = world.findNearest(super.getEntityPosition(), Stump.class);
        if (fairyTarget.isPresent()) {

            Point tgtPos = fairyTarget.get().getEntityPosition();
            if (this.moveTo(world, fairyTarget.get(), scheduler)) {

                Sapling sapling = new Sapling(Functions.SAPLING_KEY + "_" + fairyTarget.get().getEntityID(), tgtPos, imageStore.getImageList(Functions.SAPLING_KEY),
                        Functions.SAPLING_ACTION_ANIMATION_PERIOD, Functions.SAPLING_ACTION_ANIMATION_PERIOD, super.getHealth(), super.getHealthLimit());

                world.addEntity(sapling);
                sapling.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this, this.createActivityAction(world, imageStore), this.getActionPeriod());
    }

    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (super.getEntityPosition().adjacent(target.getEntityPosition())) {
            world.removeEntity(scheduler, target);
            return true;
        } else {
            Point nextPos = this.nextPosition(world, target.getEntityPosition());

            if (!super.getEntityPosition().equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }

    /**
     * SingleStepPathingStrategy
     * @param world
     * @param destPos
     * @return
     */
    public Point nextPosition(WorldModel world, Point destPos) {
        List<Point> path = new ArrayList<Point>();
        SingleStepPathingStrategy singleStep = new SingleStepPathingStrategy();
        Point nextPos;
        path = singleStep.computePath(this.getEntityPosition(), destPos,
                p -> world.withinBounds(p) && !world.isOccupied(p), //can pass through
                (p1, p2) -> p1.adjacent(p2),  //withinReach
                PathingStrategy.CARDINAL_NEIGHBORS);

        if(path.size() == 0) {

            System.out.println("No path");
            nextPos = this.getEntityPosition();
            return nextPos;
        }
        nextPos = path.get(0);
        return nextPos;
    }
}
