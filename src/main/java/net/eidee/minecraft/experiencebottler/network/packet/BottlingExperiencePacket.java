/*
 * MIT License
 *
 * Copyright (c) 2021-2023 EideeHi
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

import net.eidee.minecraft.experiencebottler.ExperienceBottlerMod;
import net.eidee.minecraft.experiencebottler.screen.ExperienceBottlerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * This class handles the packets that reflect the experience values entered by the user in the
 * client to the server.
 */
public class BottlingExperiencePacket implements FabricPacket {
  public static final PacketType<BottlingExperiencePacket> TYPE;

  static {
    TYPE =
        PacketType.create(
            ExperienceBottlerMod.identifier("bottling_experience"), BottlingExperiencePacket::new);
  }

  public final int experience;

  public BottlingExperiencePacket(int experience) {
    this.experience = experience;
  }

  public BottlingExperiencePacket(PacketByteBuf buf) {
    this(buf.readInt());
  }

  /**
   * Reflect the experience value sent by the client.
   *
   * @param packet The packet sent by the client.
   * @param player The player who sent the data
   * @param responseSender Where to send the response.
   */
  public static void receive(
      BottlingExperiencePacket packet, ServerPlayerEntity player, PacketSender responseSender) {
    if (player.currentScreenHandler instanceof ExperienceBottlerScreenHandler screenHandler) {
      screenHandler.setBottlingExperience(packet.experience);
    }
  }

  @Override
  public void write(PacketByteBuf buf) {
    buf.writeInt(experience);
  }

  @Override
  public PacketType<?> getType() {
    return TYPE;
  }
}
