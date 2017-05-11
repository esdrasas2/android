package com.crisnello.notereader.entitie;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by crisnello on 13/04/17.
 */

public class Nota implements Serializable {

    private static final long serialVersionUID = -6217582986433981000L;

    private long id;

    private String cnpj;

    private String tipo;

    private Date dataEmissao;

    //private long numeroFiscalCoo;

    private String numeroFiscalCoo;

    private double valor;

    private long idCliente;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }


    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(Date dataEmissao) {
        this.dataEmissao = dataEmissao;
    }


    public String getNumeroFiscalCoo() {
        return numeroFiscalCoo;
    }

    public void setNumeroFiscalCoo(String numeroFiscalCoo) {
        this.numeroFiscalCoo = numeroFiscalCoo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String resultadoDouble = String.format("%.2f", getValor());
        if(getCnpj() == null || getCnpj().isEmpty()){
            return "                              Adicionar 1ª Nota                            ";
        }else {
            return "CNPJ : " + getCnpj() + "       " + sdf.format(getDataEmissao()) + "     R$ " + resultadoDouble;
        }
    }
}



