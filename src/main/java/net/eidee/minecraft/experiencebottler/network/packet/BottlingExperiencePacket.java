/*
 * MIT License
 *
 * Copyright (c) 2021 EideeHi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.eidee.minecraft.experiencebottler.network.packet;

import net.eidee.minecraft.experiencebottler.network.Networks;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * This class handles the packets that reflect the experience values entered by the user in the
 * client to the server.
 */
public class BottlingExperiencePacket {
  private BottlingExperiencePacket() {}

  /** Sends the experience value passed in the argument to the server. */
  @Environment(EnvType.CLIENT)
  public static void send(int experience) {
    PacketByteBuf buf = PacketByteBufs.create();
    buf.writeInt(experience);
    ClientPlayNetworking.send(Networks.BOTTLING_EXPERIENCE, buf);
  }

  /**
   * Reflect the experience value sent by the client.
   *
   * @param server The server instance.
   * @param player The player who sent the data
   * @param handler The network handler of the player.
   * @param buf The data sent by the client.
   * @param responseSender Where to send the response.
   */
  public static void receive(
      MinecraftServer server,
      ServerPlayerEntity player,
      ServerPlayNetworkHandler handler,
      PacketByteBuf buf,
      PacketSender responseSender) {
    int experience = buf.readInt();
    server.execute(() -> {
      if (player.currentScreenHandler instanceof ExperienceBottlerScreenHandler screenHandler) {
        screenHandler.setBottlingExperience(experience);
      }
    });
  }
}
