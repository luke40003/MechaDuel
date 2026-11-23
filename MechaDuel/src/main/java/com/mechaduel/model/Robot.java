package com.mechaduel.model;

import com.mechaduel.exception.RobotInvalidoException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Robot implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private int hp;
    private int atk;
    private int def;
    private int spd;
    private int nrg;
    private List<String> efectos; // "Q", "R", "N"

    // Atributos temporales de combate
    private int turnoAcumulado;
    private int dodgeStacks;

    public Robot(String nombre, int atk, int def, int spd, int nrg, List<String> efectos) throws RobotInvalidoException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RobotInvalidoException("Debes asignarle un nombre al robot.");
        }
        if (atk < 0 || def < 0 || spd < 0 || nrg < 0) {
            throw new RobotInvalidoException("Ninguna estadística puede ser negativa.");
        }
        if (atk + def + spd + nrg != 200) {
            throw new RobotInvalidoException("La suma de ATK + DEF + SPD + NRG debe ser exactamente 200 (Actual: "
                    + (atk + def + spd + nrg) + ").");
        }
        this.nombre = nombre.trim();
        this.hp = 100;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.nrg = nrg;
        this.efectos = (efectos != null) ? new ArrayList<>(efectos) : new ArrayList<>();
        this.turnoAcumulado = 0;
        this.dodgeStacks = 0;
    }

    // Constructor privado para reconstruir el robot rival recibido por Socket
    private Robot(int hp, int atk, int def, int spd, int nrg, List<String> efectos) {
        this.nombre = "Mecha Rival";
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
        this.nrg = nrg;
        this.efectos = efectos;
        this.turnoAcumulado = 0;
        this.dodgeStacks = 0;
    }

    public void reiniciarParaCombate() {
        this.hp = 100;
        this.nrg = 200 - (this.atk + this.def + this.spd);
        this.turnoAcumulado = 0;
        this.dodgeStacks = 0;
        if (this.efectos == null) {
            this.efectos = new ArrayList<>();
        }
    }

    // Fórmula ataque normal: D = ATK^2 / (DEF + ATK)
    public int calcularDanioNormal(Robot enemigo) {
        double denominador = enemigo.getDef() + this.atk;
        if (denominador == 0) return 0;
        double danio = (double) (this.atk * this.atk) / denominador;
        return (int) Math.round(danio);
    }

    // Fórmula ataque especial: D = (ATK + 0.5 * NRG)^2 / (ATK + 0.5 * NRG + DEF)
    public int calcularDanioEspecial(Robot enemigo) {
        double atkTotal = this.atk + (0.5 * this.nrg);
        double denominador = atkTotal + enemigo.getDef();
        if (denominador == 0) return 0;
        double danio = (atkTotal * atkTotal) / denominador;
        return (int) Math.round(danio);
    }

    public void recibirDanio(int danio) {
        this.hp -= danio;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public void curar(int cantidad) {
        this.hp += cantidad;
        if (this.hp > 100) {
            this.hp = 100;
        }
    }

    public void cargarEnergia(int cantidad) {
        this.nrg += cantidad;
    }

    public void incrementarDodge() {
        if (this.dodgeStacks < 2) {
            this.dodgeStacks++;
        }
    }

    public int getProbabilidadDodge() {
        if (this.dodgeStacks == 1) return 50;
        if (this.dodgeStacks >= 2) return 75;
        return 0;
    }

    public boolean intentarEsquivar() {
        int prob = getProbabilidadDodge();
        this.dodgeStacks = 0; // Se consume al recibir un ataque
        if (prob == 0) return false;
        int aleatorio = new Random().nextInt(100) + 1; // 1 a 100
        return aleatorio <= prob;
    }

    // Estructura requerida: { HP: #valor, ATK:#valor, DEF:#valor, SPD:#valor, NRG:#valor, EFF: [ Q, R, N] }
    public String toProtocoloString() {
        String listaEff = String.join(", ", this.efectos);
        return "{ HP: " + hp + ", ATK: " + atk + ", DEF: " + def + ", SPD: " + spd + ", NRG: " + nrg + ", EFF: [" + listaEff + "] }";
    }

    public static Robot fromProtocoloString(String mensaje) {
        String limpio = mensaje.replace("{", "").replace("}", "").trim();
        int idxEff = limpio.indexOf("EFF:");
        String parteStats = limpio.substring(0, idxEff).trim();
        String parteEff = limpio.substring(idxEff + 4).replace("[", "").replace("]", "").trim();

        int hp = 100, atk = 0, def = 0, spd = 0, nrg = 0;
        String[] pares = parteStats.split(",");
        for (String par : pares) {
            String[] kv = par.trim().split(":");
            if (kv.length == 2) {
                String clave = kv[0].trim();
                int valor = Integer.parseInt(kv[1].trim());
                switch (clave) {
                    case "HP": hp = valor; break;
                    case "ATK": atk = valor; break;
                    case "DEF": def = valor; break;
                    case "SPD": spd = valor; break;
                    case "NRG": nrg = valor; break;
                }
            }
        }

        List<String> efectos = new ArrayList<>();
        if (!parteEff.isEmpty()) {
            for (String e : parteEff.split(",")) {
                if (!e.trim().isEmpty()) {
                    efectos.add(e.trim());
                }
            }
        }
        return new Robot(hp, atk, def, spd, nrg, efectos);
    }

    @Override
    public String toString() {
        return nombre + " (ATK:" + atk + " DEF:" + def + " SPD:" + spd + " NRG:" + nrg + " " + efectos + ")";
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getSpd() { return spd; }
    public int getNrg() { return nrg; }
    public void setNrg(int nrg) { this.nrg = nrg; }
    public List<String> getEfectos() { return efectos; }
    public int getTurnoAcumulado() { return turnoAcumulado; }
    public void setTurnoAcumulado(int turnoAcumulado) { this.turnoAcumulado = turnoAcumulado; }
}
