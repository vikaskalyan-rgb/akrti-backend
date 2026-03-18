package com.akriti.apartment.config;

import com.akriti.apartment.entity.Flat;
import com.akriti.apartment.entity.User;
import com.akriti.apartment.repository.FlatRepository;
import com.akriti.apartment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired private FlatRepository flatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (flatRepository.count() == 0) {
            log.info("🌱 Seeding flat data...");
            List<Flat> flats = buildFlats();
            flatRepository.saveAll(flats);
            log.info("✅ Seeded {} flats", flats.size());
        } else {
            log.info("Flats already seeded — skipping flat seed");
        }

        // Always run password seeding — safe (only creates/updates if needed)
        seedUserPasswords();
    }

    private void seedUserPasswords() {
        log.info("🔐 Seeding user passwords...");
        List<Flat> flats = flatRepository.findAll();
        int created = 0, updated = 0;

        for (Flat flat : flats) {
            if (flat.getFloor() == 0) continue;

            // ── Owner / Admin user ──────────────────────────────────
            String ownerName = (flat.getOwnerName() != null
                    && !flat.getOwnerName().equals("Unknown")
                    && !flat.getOwnerName().isBlank())
                    ? flat.getOwnerName() : null;

            if (ownerName != null) {
                String identifier = flat.getFlatNo(); // e.g. "2H"
                String defaultPw  = flat.getFlatNo() + "@123";

                User.Role role = User.Role.OWNER;
                if (List.of("2H","4B","4J","2J").contains(flat.getFlatNo()))
                    role = User.Role.ADMIN;

                Optional<User> existing = userRepository.findByIdentifier(identifier);
                if (existing.isEmpty()) {
                    User user = User.builder()
                            .flatNo(flat.getFlatNo())
                            .identifier(identifier)
                            .name(ownerName)
                            .role(role)
                            .phone(flat.getOwnerPhone())
                            .email(flat.getOwnerEmail())
                            .passwordHash(passwordEncoder.encode(defaultPw))
                            .firstLogin(true)
                            .isActive(true)
                            .build();
                    userRepository.save(user);
                    log.info("✅ Owner user: {} / {}", identifier, defaultPw);
                    created++;
                } else {
                    User u = existing.get();
                    if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                        u.setPasswordHash(passwordEncoder.encode(defaultPw));
                        u.setEmail(flat.getOwnerEmail());
                        u.setFirstLogin(true);
                        userRepository.save(u);
                        updated++;
                    }
                }
            }

            // ── Tenant user (only for rented flats) ─────────────────
            if (flat.getOwnerType() == Flat.OwnerType.RENTED
                    && flat.getResidentName() != null
                    && !flat.getResidentName().isBlank()) {

                String identifier = flat.getFlatNo() + "_tenant"; // e.g. "4B_tenant"
                String defaultPw  = flat.getFlatNo() + "_tenant@123";

                Optional<User> existing = userRepository.findByIdentifier(identifier);
                if (existing.isEmpty()) {
                    User user = User.builder()
                            .flatNo(flat.getFlatNo())
                            .identifier(identifier)
                            .name(flat.getResidentName())
                            .role(User.Role.TENANT)
                            .phone(flat.getResidentPhone())
                            .email(flat.getResidentEmail())
                            .passwordHash(passwordEncoder.encode(defaultPw))
                            .firstLogin(true)
                            .isActive(true)
                            .build();
                    userRepository.save(user);
                    log.info("✅ Tenant user: {} / {}", identifier, defaultPw);
                    created++;
                } else {
                    User u = existing.get();
                    if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                        u.setPasswordHash(passwordEncoder.encode(defaultPw));
                        u.setEmail(flat.getResidentEmail());
                        u.setFirstLogin(true);
                        userRepository.save(u);
                        updated++;
                    }
                }
            }
        }

        // ── Supervisor ───────────────────────────────────────────────
        if (userRepository.findByIdentifier("SUP").isEmpty()) {
            User supervisor = User.builder()
                    .identifier("SUP")
                    .name("Sharbudeen")
                    .role(User.Role.ADMIN)
                    .phone("9150625740")
                    .passwordHash(passwordEncoder.encode("SUP@123"))
                    .firstLogin(true)
                    .isActive(true)
                    .build();
            userRepository.save(supervisor);
            log.info("✅ Supervisor: SUP / SUP@123");
        }

        log.info("✅ Users: {} created, {} updated", created, updated);
    }

    private List<Flat> buildFlats() {
        return List.of(
                // ── Floor 1 ──────────────────────────────────────────────
                flat("1A",1,"A","North", Flat.OwnerType.OWNER_OCCUPIED, "Ramesh Kumar",       "9840012345", "ramesh.kumar@gmail.com",       "Ramesh Kumar",        "9840012345", "ramesh.kumar@gmail.com",       "P1"),
                flat("1B",1,"B","North", Flat.OwnerType.RENTED,         "Suresh Babu",         "9840023456", "suresh.babu@gmail.com",         "Priya Venkat",        "9840034567", "priya.venkat@gmail.com",       "P2"),
                flat("1C",1,"C","North", Flat.OwnerType.RENTED,         "Anand Krishnan",      "9840045678", "anand.krishnan@gmail.com",      "Suresh Iyer",         "9840056789", "suresh.iyer@gmail.com",        "P3"),
                flat("1D",1,"D","North", Flat.OwnerType.OWNER_OCCUPIED, "Meena Sharma",        "9840067890", "meena.sharma@gmail.com",        "Meena Sharma",        "9840067890", "meena.sharma@gmail.com",       "P4"),
                flat("1E",1,"E","North", Flat.OwnerType.OWNER_OCCUPIED, "Anil Reddy",          "9840078901", "anil.reddy@gmail.com",          "Anil Reddy",          "9840078901", "anil.reddy@gmail.com",         "P5"),
                flat("1F",1,"F","North", Flat.OwnerType.RENTED,         "Gopinath Warrier",    "9840089012", "gopinath.warrier@gmail.com",    "Kavitha Nair",        "9840090123", "kavitha.nair@gmail.com",       "P6"),
                flat("1G",1,"G","South", Flat.OwnerType.OWNER_OCCUPIED, "Vinod Pillai",        "9840101234", "vinod.pillai@gmail.com",        "Vinod Pillai",        "9840101234", "vinod.pillai@gmail.com",       "P7"),
                flat("1H",1,"H","South", Flat.OwnerType.RENTED,         "Sridhar Nair",        "9840112345", "sridhar.nair@gmail.com",        "Sunita Rao",          "9840123456", "sunita.rao@gmail.com",         "P8"),
                flat("1J",1,"J","South", Flat.OwnerType.OWNER_OCCUPIED, "Rajan Menon",         "9840134567", "rajan.menon@gmail.com",         "Rajan Menon",         "9840134567", "rajan.menon@gmail.com",        "P9"),
                flat("1K",1,"K","South", Flat.OwnerType.OWNER_OCCUPIED, "Deepa Krishnan",      "9840145678", "deepa.krishnan@gmail.com",      "Deepa Krishnan",      "9840145678", "deepa.krishnan@gmail.com",     "P10"),
                // ── Floor 2 ──────────────────────────────────────────────
                flat("2A",2,"A","North", Flat.OwnerType.OWNER_OCCUPIED, "Anand Bose",          "9840156789", "anand.bose@gmail.com",          "Anand Bose",          "9840156789", "anand.bose@gmail.com",         "P11"),
                flat("2B",2,"B","North", Flat.OwnerType.RENTED,         "Prakash Hegde",       "9840167890", "prakash.hegde@gmail.com",       "Lakshmi Subramanian", "9840178901", "lakshmi.subramanian@gmail.com","P12"),
                flat("2C",2,"C","North", Flat.OwnerType.OWNER_OCCUPIED, "Mohan Das",           "9840189012", "mohan.das@gmail.com",           "Mohan Das",           "9840189012", "mohan.das@gmail.com",          "P13"),
                flat("2D",2,"D","North", Flat.OwnerType.OWNER_OCCUPIED, "Radha Gopal",         "9840190123", "radha.gopal@gmail.com",         "Radha Gopal",         "9840190123", "radha.gopal@gmail.com",        "P14"),
                flat("2E",2,"E","North", Flat.OwnerType.VACANT,         "Sanjay Mehta",        "9840201234", "sanjay.mehta@gmail.com",        null, null, null,                                                    "P15"),
                flat("2F",2,"F","North", Flat.OwnerType.RENTED,         "Vijay Shetty",        "9840212345", "vijay.shetty@gmail.com",        "Usha Patel",          "9840223456", "usha.patel@gmail.com",         "P16"),
                flat("2G",2,"G","South", Flat.OwnerType.RENTED,         "Bhaskar Reddy",       "9840234567", "bhaskar.reddy@gmail.com",       "Harish Babu",         "9840245678", "harish.babu@gmail.com",        "P17"),
                flat("2H",2,"H","South", Flat.OwnerType.OWNER_OCCUPIED, "Vikas",               "7010033792", "vikas@gmail.com",               "Vikas",               "7010033792", "vikas@gmail.com",              "P18"),
                flat("2J",2,"J","South", Flat.OwnerType.OWNER_OCCUPIED, "Ranjith",             "9790088048", "ranjith@gmail.com",             "Ranjith",             "9790088048", "ranjith@gmail.com",            "P19"),
                flat("2K",2,"K","South", Flat.OwnerType.OWNER_OCCUPIED, "Saritha Nambiar",     "9840289012", "saritha.nambiar@gmail.com",     "Saritha Nambiar",     "9840289012", "saritha.nambiar@gmail.com",    "P20"),
                // ── Floor 3 ──────────────────────────────────────────────
                flat("3A",3,"A","North", Flat.OwnerType.RENTED,         "Dinesh Shetty",       "9840290123", "dinesh.shetty@gmail.com",       "Asha Thomas",         "9840301234", "asha.thomas@gmail.com",        "P21"),
                flat("3B",3,"B","North", Flat.OwnerType.OWNER_OCCUPIED, "Rajan Menon",         "9840312345", "rajan.menon2@gmail.com",        "Rajan Menon",         "9840312345", "rajan.menon2@gmail.com",       "P22"),
                flat("3C",3,"C","North", Flat.OwnerType.OWNER_OCCUPIED, "Kaveri Sharma",       "9840323456", "kaveri.sharma@gmail.com",       "Kaveri Sharma",       "9840323456", "kaveri.sharma@gmail.com",      "P23"),
                flat("3D",3,"D","North", Flat.OwnerType.RENTED,         "Murali Menon",        "9840334567", "murali.menon@gmail.com",        "Parvathi Nambiar",    "9840345678", "parvathi.nambiar@gmail.com",   "P24"),
                flat("3E",3,"E","North", Flat.OwnerType.OWNER_OCCUPIED, "Ravi Pillai",         "9840356789", "ravi.pillai@gmail.com",         "Ravi Pillai",         "9840356789", "ravi.pillai@gmail.com",        "P25"),
                flat("3F",3,"F","North", Flat.OwnerType.OWNER_OCCUPIED, "Sudha Bose",          "9840367890", "sudha.bose@gmail.com",          "Sudha Bose",          "9840367890", "sudha.bose@gmail.com",         "P26"),
                flat("3G",3,"G","South", Flat.OwnerType.RENTED,         "Venkat Iyer",         "9840378901", "venkat.iyer@gmail.com",         "Chandrika Rao",       "9840389012", "chandrika.rao@gmail.com",      "P27"),
                flat("3H",3,"H","South", Flat.OwnerType.OWNER_OCCUPIED, "Smitha Reddy",        "9840390123", "smitha.reddy@gmail.com",        "Smitha Reddy",        "9840390123", "smitha.reddy@gmail.com",       "P28"),
                flat("3J",3,"J","South", Flat.OwnerType.OWNER_OCCUPIED, "Swaminathan",         "9789980216", "swaminathan@gmail.com",         "Swaminathan",         "9789980216", "swaminathan@gmail.com",        "P29"),
                flat("3K",3,"K","South", Flat.OwnerType.RENTED,         "Leela Menon",         "9840412345", "leela.menon@gmail.com",         "Prakash Hegde",       "9840423456", "prakash.hegde3@gmail.com",     "P30"),
                // ── Floor 4 ──────────────────────────────────────────────
                flat("4A",4,"A","North", Flat.OwnerType.OWNER_OCCUPIED, "Sanjay Mehta",        "9840434567", "sanjay.mehta2@gmail.com",       "Sanjay Mehta",        "9840434567", "sanjay.mehta2@gmail.com",      "P31"),
                flat("4B",4,"B","North", Flat.OwnerType.RENTED,         "Kalyan",              "9994445388", "kalyan@gmail.com",              "Prasanna",            "9840330918", "prasanna@gmail.com",           "P32"),
                flat("4C",4,"C","North", Flat.OwnerType.OWNER_OCCUPIED, "Padma Iyer",          "9840467890", "padma.iyer@gmail.com",          "Padma Iyer",          "9840467890", "padma.iyer@gmail.com",         "P33"),
                flat("4D",4,"D","North", Flat.OwnerType.OWNER_OCCUPIED, "Manjula Das",         "9840478901", "manjula.das@gmail.com",         "Manjula Das",         "9840478901", "manjula.das@gmail.com",        "P34"),
                flat("4E",4,"E","North", Flat.OwnerType.RENTED,         "Chandrika Rao",       "9840489012", "chandrika.rao2@gmail.com",      "Vijay Shetty",        "9840490123", "vijay.shetty2@gmail.com",      "P35"),
                flat("4F",4,"F","North", Flat.OwnerType.RENTED,         "Rekha Pillai",        "9840501234", "rekha.pillai@gmail.com",        "Sunil Kumar",         "9840512345", "sunil.kumar@gmail.com",        "P36"),
                flat("4G",4,"G","South", Flat.OwnerType.OWNER_OCCUPIED, "Ravi Pillai",         "9840523456", "ravi.pillai2@gmail.com",        "Ravi Pillai",         "9840523456", "ravi.pillai2@gmail.com",       "P37"),
                flat("4H",4,"H","South", Flat.OwnerType.RENTED,         "Geetha Krishnan",     "9840534567", "geetha.krishnan2@gmail.com",    "Sunita Rao",          "9840545678", "sunita.rao2@gmail.com",        "P38"),
                flat("4J",4,"J","South", Flat.OwnerType.OWNER_OCCUPIED, "Murali",              "9600699366", "murali@gmail.com",              "Murali",              "9600699366", "murali@gmail.com",             "P39"),
                flat("4K",4,"K","South", Flat.OwnerType.VACANT,         "Dinesh Shetty",       "9840578901", "dinesh.shetty2@gmail.com",      null, null, null,                                                    "P40"),
                // ── Ground Floor ──────────────────────────────────────────
                flat("GF1",0,"GF1","Ground", Flat.OwnerType.OWNER_OCCUPIED, "Society", "9840000001", null, "Society Office", "9840000001", null, null),
                flat("GF2",0,"GF2","Ground", Flat.OwnerType.OWNER_OCCUPIED, "Society", "9840000002", null, "Security Post",  "9840000002", null, null),
                flat("GF3",0,"GF3","Ground", Flat.OwnerType.VACANT,         "Society", "0000000000", null, null, null, null, null)
        );
    }

    private Flat flat(String flatNo, int floor, String unit, String wing,
                      Flat.OwnerType ownerType,
                      String ownerName, String ownerPhone, String ownerEmail,
                      String residentName, String residentPhone, String residentEmail,
                      String parkingSlot) {
        return Flat.builder()
                .flatNo(flatNo).floor(floor).unit(unit).wing(wing)
                .ownerType(ownerType)
                .ownerName(ownerName).ownerPhone(ownerPhone).ownerEmail(ownerEmail)
                .residentName(residentName).residentPhone(residentPhone).residentEmail(residentEmail)
                .parkingSlot(parkingSlot)
                .isActive(true)
                .build();
    }
}