package com.example.textconverter.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class ModelPdfView {

    Uri pdfuri;
    int pageNumber;
    int pageCount;
    Bitmap bitmap;


    public ModelPdfView(Uri pdfuri, int pageNumber, int pageCount, Bitmap bitmap) {
        this.pdfuri = pdfuri;
        this.pageNumber = pageNumber;
        this.pageCount = pageCount;
        this.bitmap = bitmap;
    }

    public Uri getPdfuri() {
        return pdfuri;
    }

    public void setPdfuri(Uri pdfuri) {
        this.pdfuri = pdfuri;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
