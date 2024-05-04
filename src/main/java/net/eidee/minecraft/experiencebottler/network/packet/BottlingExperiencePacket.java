/*
 * MIT License
 *
 * Copyright (c) 2021-2024 EideeHi
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

import io.netty.buffer.ByteBuf;
import net.eidee.minecraft.experiencebottler.ExperienceBottlerMod;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * This class handles the packets that reflect the experience values entered by the user in the
 * client to the server.
 */
public record BottlingExperiencePacket(int experience) implements CustomPayload {
  public static final CustomPayload.Id<BottlingExperiencePacket> ID;
  public static final PacketCodec<ByteBuf, BottlingExperiencePacket> CODEC;

  static {
    ID = new Id<>(ExperienceBottlerMod.identifier("bottling_experience"));
    CODEC =
        PacketCodecs.VAR_INT.xmap(
            BottlingExperiencePacket::new, BottlingExperiencePacket::experience);
  }

  /**
   * Reflect the experience value sent by the client.
   *
   * @param payload The experience value sent by the client.
   * @param context The context of the packet.
   */
  public static void receive(
      BottlingExperiencePacket payload, ServerPlayNetworking.Context context) {
    if (context.player().currentScreenHandler
        instanceof ExperienceBottlerScreenHandler screenHandler) {
      screenHandler.setBottlingExperience(payload.experience());
    }
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
