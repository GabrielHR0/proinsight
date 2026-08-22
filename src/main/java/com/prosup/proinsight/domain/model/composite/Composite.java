package com.prosup.proinsight.domain.model.composite;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.teste.Teste;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Composite implements Component {

    private List<Component> children = new ArrayList<>();

    public void add(Component child) {
        if (child == null) {
            throw new IllegalArgumentException("child cannot be null");
        }
        children.add(child);
    }

    public void remove(Component child) {
        children.remove(child);
    }

    public List<Component> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void setChildren(List<Component> children) {
        this.children = children == null ? new ArrayList<>() : new ArrayList<>(children);
    }

    @Override
    public Leaf classificar() {
        for (Component child : children) {
            Leaf result = child.classificar();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Leaf classificarComTeste(Teste teste) {
        return classificarComTeste(teste, null);
    }

    @Override
    public Leaf classificarComTeste(Teste teste, DadosAvaliacao dados) {
        for (Component child : children) {
            Leaf result = child.classificarComTeste(teste, dados);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
