package uk.ac.ebi.intact.application.hierarchView.business.image;

import java.io.*;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

public class DOMRasterizer {

  /**
   * Create a SVG DOM Document
   */
  public Document createDocument() {
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document document = impl.createDocument(svgNS, "svg", null);
    Element root = document.getDocumentElement();
    root.setAttributeNS(null, "width", "450");
    root.setAttributeNS(null, "height", "500");

    Element e;
    e = document.createElementNS(svgNS, "rect");
    e.setAttributeNS(null, "x", "10");
    e.setAttributeNS(null, "y", "10");
    e.setAttributeNS(null, "width", "200");
    e.setAttributeNS(null, "height", "300");
    e.setAttributeNS(null, "style", "fill:red;stroke:black;stroke-width:4");
    root.appendChild(e);

    e = document.createElementNS(svgNS, "circle");
    e.setAttributeNS(null, "cx", "225");
    e.setAttributeNS(null, "cy", "250");
    e.setAttributeNS(null, "r", "100");
    e.setAttributeNS(null, "style", "fill:green;fill-opacity:.5");
    root.appendChild(e);

    return document;
  }


  /**
   * Save the document to JPEG
   * @param document the ducoment to convert
   */
  public void save(Document document) throws Exception {
    JPEGTranscoder t = new JPEGTranscoder();
    t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
			 new Float(.8));
    TranscoderInput input = new TranscoderInput(document);
    OutputStream ostream = new FileOutputStream("out.jpg");
    TranscoderOutput output = new TranscoderOutput(ostream);
    t.transcode(input, output);
    ostream.flush();
    ostream.close();
  }


  /**
   * Demo
   *
   */
  public static void main(String [] args) throws Exception {
    DOMRasterizer rasterizer = new DOMRasterizer();
    Document document = rasterizer.createDocument();
    rasterizer.save(document);
    System.exit(0);
  }
} // DOMRasterizer


