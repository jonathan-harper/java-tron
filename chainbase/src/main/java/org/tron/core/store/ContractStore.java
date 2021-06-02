package org.tron.core.store;

import com.google.common.collect.Streams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.AbiCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j(topic = "DB")
@Component
public class ContractStore extends TronStoreWithRevoking<ContractCapsule> {

  private static final ExecutorService workers
      = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);

  @Autowired
  private AbiStore abiStore;

  @Autowired
  private ContractStore(@Value("contract") String dbName) {
    super(dbName);
  }

  @Override
  public ContractCapsule get(byte[] key) {
    ContractCapsule contractCapsule = getUnchecked(key);
    if (contractCapsule == null) {
      return null;
    }
    if (contractCapsule.hasABI()) {
      abiStore.put(key, new AbiCapsule(contractCapsule));
      this.put(key, new ContractCapsule(
          contractCapsule.getInstance().toBuilder().clearAbi().build()));
    } else {
      AbiCapsule abiCapsule = abiStore.get(key);
      if (abiCapsule != null) {
        contractCapsule.setABI(abiCapsule.getInstance());
      }
    }
    return contractCapsule;
  }

  public ContractCapsule getWithoutAbi(byte[] key) {
    ContractCapsule contractCapsule = getUnchecked(key);
    if (contractCapsule == null) {
      return null;
    }
    if (contractCapsule.hasABI()) {
      moveAbi(key, contractCapsule);
      contractCapsule.clearABI();
    }
    return contractCapsule;
  }

  private void moveAbi(byte[] key, ContractCapsule originContract) {
    AbiCapsule abiCapsule = new AbiCapsule(originContract);
    ContractCapsule contractCapsule = new ContractCapsule(
        originContract.getInstance().toBuilder().clearAbi().build());
    workers.submit(() -> {
      abiStore.put(key, abiCapsule);
      put(key, contractCapsule);
    });
  }

  /**
   * get total transaction.
   */
  public long getTotalContracts() {
    return Streams.stream(revokingDB.iterator()).count();
  }

  /**
   * find a transaction  by it's id.
   */
  public byte[] findContractByHash(byte[] trxHash) {
    return revokingDB.getUnchecked(trxHash);
  }

  /**
   *
   * @param contractAddress
   * @return
   */
  public SmartContract.ABI getABI(byte[] contractAddress) {
    ContractCapsule contractCapsule = get(contractAddress);
    if (contractCapsule == null) {
      return null;
    }

    return contractCapsule.getInstance().getAbi();
  }

}
