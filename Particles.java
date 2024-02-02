package wtf.expensive.modules.impl.combat;

import java.awt.Color;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector2d;
import wtf.expensive.events.Event;
import wtf.expensive.events.impl.player.EventMotion;
import wtf.expensive.events.impl.render.EventRender;
import wtf.expensive.modules.Function;
import wtf.expensive.modules.FunctionAnnotation;
import wtf.expensive.modules.Type;
import wtf.expensive.util.math.PlayerPositionTracker;
import wtf.expensive.util.render.BloomHelper;
import wtf.expensive.util.render.ColorUtil;
import wtf.expensive.util.render.ProjectionUtils;
import wtf.expensive.util.render.RenderUtil.Render2D;

@FunctionAnnotation(
   name = "DParticles",
   type = Type.Combat
)
public class Particles extends Function {
   CopyOnWriteArrayList<wtf.expensive.modules.impl.combat.Particles.Point> points = new CopyOnWriteArrayList();

   public void onEvent(Event event) {
      Iterator var3;
      if (event instanceof EventMotion) {
         EventMotion e = (EventMotion)event;
         var3 = mc.world.getAllEntities().iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            if (entity instanceof LivingEntity) {
               LivingEntity l = (LivingEntity)entity;
               if (l.hurtTime == 9) {
                  this.createPoints(l.getPositionVec().add(0.0D, 1.0D, 0.0D));
               }
            }

            if (entity instanceof EnderPearlEntity) {
               EnderPearlEntity p = (EnderPearlEntity)entity;
               this.points.add(new wtf.expensive.modules.impl.combat.Particles.Point(this, p.getPositionVec()));
            }
         }
      }

      if (event instanceof EventRender) {
         EventRender e = (EventRender)event;
         if (e.isRender2D()) {
            if (this.points.size() > 100) {
               this.points.remove(0);
            }

            var3 = this.points.iterator();

            while(true) {
               while(var3.hasNext()) {
                  wtf.expensive.modules.impl.combat.Particles.Point point = (wtf.expensive.modules.impl.combat.Particles.Point)var3.next();
                  long alive = System.currentTimeMillis() - point.createdTime;
                  if (alive <= point.aliveTime && mc.player.canVectorBeSeenFixed(point.position) && PlayerPositionTracker.isInView(point.position)) {
                     Vector2d pos = ProjectionUtils.project(point.position.x, point.position.y, point.position.z);
                     if (pos != null) {
                        float sizeDefault = point.size;
                        point.update();
                        float size = 1.0F - (float)alive / (float)point.aliveTime;
                        BloomHelper.registerRenderCall(() -> {
                           Render2D.drawRoundCircle((float)pos.x, (float)pos.y, (sizeDefault + 1.0F) * size, Color.BLACK.getRGB());
                           Render2D.drawRoundCircle((float)pos.x, (float)pos.y, sizeDefault * size, ColorUtil.getColorStyle((float)this.points.indexOf(point)));
                        });
                        Render2D.drawRoundCircle((float)pos.x, (float)pos.y, (sizeDefault + 1.0F) * size, Color.BLACK.getRGB());
                        Render2D.drawRoundCircle((float)pos.x, (float)pos.y, sizeDefault * size, ColorUtil.getColorStyle((float)this.points.indexOf(point)));
                     }
                  } else {
                     this.points.remove(point);
                  }
               }

               return;
            }
         }
      }

   }

   private void createPoints(Vector3d position) {
      for(int i = 0; i < ThreadLocalRandom.current().nextInt(5, 20); ++i) {
         this.points.add(new wtf.expensive.modules.impl.combat.Particles.Point(this, position));
      }

   }
}
