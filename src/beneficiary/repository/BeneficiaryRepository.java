package beneficiary.repository;

import beneficiary.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/** File-based persistence for the prototype. All updates are staged, then saved before publishing. */
public final class BeneficiaryRepository {
    public static final class Data implements Serializable {
        private static final long serialVersionUID = 1L;
        public int nextId=1;
        public final Map<Integer, Beneficiary> beneficiaries=new LinkedHashMap<>();
        public final Map<Integer, NeedAssessment> needs=new LinkedHashMap<>();
        public final Map<Integer, AssistanceRequest> requests=new LinkedHashMap<>();
        public final Map<Integer, Resource> resources=new LinkedHashMap<>();
        public final Map<Integer, ResourceDistribution> distributions=new LinkedHashMap<>();
        public final Map<Integer, ServiceRecord> services=new LinkedHashMap<>();
        public final Map<Integer, ImpactRecord> impacts=new LinkedHashMap<>();
        private Data copy() {
            Data copy=new Data(); copy.nextId=nextId;
            copy.beneficiaries.putAll(beneficiaries); copy.needs.putAll(needs);
            copy.requests.putAll(requests); copy.resources.putAll(resources);
            copy.distributions.putAll(distributions); copy.services.putAll(services);
            copy.impacts.putAll(impacts); return copy;
        }
    }
    private final Path file;
    private Data current;
    public BeneficiaryRepository(Path file) {
        this.file=Objects.requireNonNull(file).toAbsolutePath();
        this.current=load();
    }
    private Data load() {
        if (!Files.exists(file)) return new Data();
        try (ObjectInputStream in=new ObjectInputStream(Files.newInputStream(file))) {
            Object value=in.readObject();
            if (!(value instanceof Data)) throw new IOException("Invalid beneficiary data file");
            return (Data)value;
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot open beneficiary data at " + file, ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Incompatible beneficiary data file", ex);
        }
    }
    private void save(Data draft) {
        Path tmp=null;
        try {
            Files.createDirectories(file.getParent());
            tmp=Files.createTempFile(file.getParent(), "beneficiary-", ".tmp");
            try (ObjectOutputStream out=new ObjectOutputStream(Files.newOutputStream(tmp))) {
                out.writeObject(draft);
            }
            try {
                Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not save beneficiary data at " + file, ex);
        } finally {
            if (tmp!=null) try { Files.deleteIfExists(tmp); } catch (IOException ignored) { }
        }
    }
    public synchronized <T> T read(Function<Data,T> work) { return work.apply(current); }
    public synchronized <T> T change(Function<Data,T> work) {
        Data draft=current.copy();
        T result=work.apply(draft); // validation errors never modify current
        save(draft);
        current=draft;
        return result;
    }
}
