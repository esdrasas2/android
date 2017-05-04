package com.crisnello.notereader;
public class ItemListView
{
    private String texto;

    public ItemListView()
    {
    }

    public ItemListView(String texto)
    {
        this.texto = texto;
    }


    public String getTexto()
    {
        return texto;
    }

    public void setTexto(String texto)
    {
        this.texto = texto;
    }
}