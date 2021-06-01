/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core.capsule;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract.ABI;


@Slf4j(topic = "capsule")
public class AbiCapsule implements ProtoCapsule<ABI> {

  private ABI abi;

  public AbiCapsule(byte[] data) {
    try {
      this.abi = ABI.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }
  }

  public AbiCapsule(ContractCapsule contract) {
    this.abi = contract.getInstance().getAbi().toBuilder().build();
  }

  public AbiCapsule(ABI abi) {
    this.abi = abi;
  }

  @Override
  public byte[] getData() {
    return this.abi.toByteArray();
  }

  @Override
  public ABI getInstance() {
    return this.abi;
  }

  @Override
  public String toString() {
    return this.abi.toString();
  }
}
