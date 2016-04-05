package de.hanneseilers.easyprinter;

import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPageable;

public class EasyPrinter {

	private String mContent = null;
	private String mHeader = null;
	private String mFooter = null;
	
	private PDRectangle mPageFormat = PDRectangle.A4;
	
	private int mFontSize = 12;
	private PDFont mFont = PDType1Font.HELVETICA;	
	private int mHeaderFontSize = 20;
	private PDFont mHeaderFont = PDType1Font.HELVETICA_BOLD;
	private int mFooterFontSize = 10;
	private PDFont mFooterFont = PDType1Font.HELVETICA;
	
	private float mBorderTop = (20 * 72.0f) / 25.4f;
	private float mBoderBottom = (20 * 72.0f) / 25.4f;
	private float mBorderLeft = (20 * 72.0f) / 25.4f;
	
	/**
	 * Constructor using a header and a footer text
	 * @param content
	 * @param header
	 * @param footer
	 */
	public EasyPrinter(String content, String header, String footer) {
		mContent = content;
		
		if( header != null )
			mHeader = header;
		if( footer != null )
			mFooter = footer;
	}
	
	/**
	 * Constructor using header text (no footer)
	 * @param content
	 * @param header
	 */
	public EasyPrinter(String content, String header){
		this(content, header, null);
	}
	
	/**
	 * Constructor using content only (no heade ror footer)
	 * @param content
	 */
	public EasyPrinter(String content){
		this(content, null, null);
	}
	
	
	
	/**
	 * @return	{@link LinkedList} of {@link String} array of lines of page header.
	 */
	private LinkedList<String> getHeaderStrings(){
		if( mHeader != null ){
			return new LinkedList<String>( Arrays.asList(mHeader.split("\n")) );
		}
		
		return new LinkedList<String>();
	}	
	
	/**
	 * @return {@link LinkedList} of {@link String} array of lines of page footer.
	 */
	private LinkedList<String> getFooterStrings(){
		if( mFooter != null ){
			return new LinkedList<String>( Arrays.asList(mFooter.split("\n")) );
		}
		
		return new LinkedList<String>();
	}
	
	/**
	 * @return {@link LinkedList} of {@link String} array of lines of page footer.
	 */
	private LinkedList<String> getContentStrings(){		
		if( mContent != null ){
			return new LinkedList<String>( Arrays.asList(mContent.split("\n")) );
		}
		
		return new LinkedList<String>();
	}
	
	/**
	 * Calculates number of lines on one page.
	 * Requires all parameters like footer, header, page format and font sizes set.
	 * @return	{@link Integer} of maximum text lines on one page.
	 */
	public int getMaxLines(){
		// page and max lines in page
		PDPage vPage = new PDPage( mPageFormat );
		float vLines = vPage.getMediaBox().getHeight();;
		
		// subtract borders
		vLines -= (mBorderTop + mBoderBottom);
		
		// get header and footer content
		LinkedList<String> vHeaderStrings = getHeaderStrings();
		LinkedList<String> vFooterStrings = getFooterStrings();
		
		// subtract header and footer lines
		vLines -= (vHeaderStrings.size() * mHeaderFontSize + mHeaderFontSize);
		vLines -= (vFooterStrings.size() * mFooterFontSize + mFontSize);
		
		vLines = vLines / mFontSize;
		
		return (int)vLines;
	}
	
	private float getCenterXPosition(String aText, PDFont aFont, float aFontSize, float aMaxWidth){
		float vPositionX = 0;
		try {
			
			float vTextWidth = aFont.getStringWidth(aText) / 1000f * aFontSize;
			if( vTextWidth < aMaxWidth ){
				vPositionX = (aMaxWidth - vTextWidth) / 2.0f;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return vPositionX;
	}
	
	/**
	 * Prints page
	 * @return	{@code true} if printed successfull, {@code false} otherwise.
	 */
	public boolean print(){
		
		try{
			
			// get content lines
			LinkedList<String> vContentStrings = getContentStrings();
			LinkedList<String> vHeaderStrings = getHeaderStrings();
			LinkedList<String> vFooterStrings = getFooterStrings();
			final int vMaxLines = getMaxLines();
			final float vPageHeight = new PDPage(mPageFormat).getMediaBox().getHeight();
			final float vMaxTextWidth = new PDPage(mPageFormat).getMediaBox().getWidth() - 2*mBorderLeft;
			float vLinesLeft = 0;
			float vPositionY = vPageHeight;
			
			// create document, page and stream objects
			PDDocument vDocument = new PDDocument();
			PDPage vPage = null;
			PDPageContentStream vContent = null;
			
			while( !vContentStrings.isEmpty() ){
				
				// check if to add new page
				if( vLinesLeft == 0 ){
					
					// end last page
					if( vContent != null ){
						vContent.endText();
						vContent.close();
					}
					
					// create new page and content stream
					vPage = new PDPage(mPageFormat);
					vDocument.addPage(vPage);
					vContent = new PDPageContentStream(vDocument, vPage);
					vContent.beginText();
					
					// set header position and font
					vContent.newLineAtOffset(mBorderLeft, vPageHeight-mBorderTop-mHeaderFontSize);
					vPositionY = vPageHeight-mBorderTop-mHeaderFontSize;
					vContent.setFont(mHeaderFont, mHeaderFontSize);
					
					// add header
					for( String vString : vHeaderStrings ){
						float vPositionX = getCenterXPosition(vString, mFont, mFontSize, vMaxTextWidth);
						vContent.newLineAtOffset(vPositionX, 0);
						vContent.showText( vString );
						vContent.newLineAtOffset(-vPositionX, -mHeaderFontSize);
						vPositionY -= mHeaderFontSize;
					}
					
					vLinesLeft = vMaxLines;
					
					// set page text font
					vContent.setFont(mFont, mFontSize);
					vContent.newLineAtOffset(0, -mHeaderFontSize);
					vPositionY -= mHeaderFontSize;
					
				}
					
				// add content lines
				vContent.showText( vContentStrings.pop() );
				vContent.newLineAtOffset(0, -mFontSize);
				vPositionY -= mFontSize;
				vLinesLeft--;
					
				// check if to add footer
				if( vLinesLeft == 0 || vContentStrings.isEmpty() ){
					
					// get difference between footer start position and current position
					// move cursor to that position
					float vPositionFooterStartY = mBoderBottom + mFooterFontSize * vFooterStrings.size();
					vContent.newLineAtOffset(0, -(vPositionY-vPositionFooterStartY));
					
					// set footer font
					vContent.setFont(mFooterFont, mFooterFontSize);
					
					// add footer
					for( String vString : vFooterStrings ){
						float vPositionX = getCenterXPosition(vString, mFooterFont, mFooterFontSize, vMaxTextWidth);
						vContent.newLineAtOffset(vPositionX, 0);
						vContent.showText(vString);
						vContent.newLineAtOffset(-vPositionX, -mFooterFontSize);
					}
					
				}
				
			}
			
			// close content stream
			vContent.endText();
			vContent.close();
			
			// print document
			PrinterJob vPrinterJob = PrinterJob.getPrinterJob();
			vPrinterJob.setPageable( new PDFPageable(vDocument) );
			if( vPrinterJob.printDialog() ){
				vPrinterJob.print();
				vDocument.close();
				return true;			
			}
			
//			vDocument.save("out.pdf");
			vDocument.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return false;		
	}


	
	/**
	 * @return	Page content {@link String} if set, {@code null} otehrwise.
	 */
	public String getContent(){
		return mContent;
	}
	
	/**
	 * Sets page content.
	 * @param aContent	{@link String} of content text.
	 */
	public void setContent(String aContent){
		mContent = aContent;
	}
	
	/**
	 * @return	Page header {@link String} if set, {@code null} otherwise.
	 */
	public String getHeader() {
		return mHeader;
	}

	/**
	 * Sets page header.
	 * @param aHeader	{@link String} of header text.
	 */
	public void setHeader(String aHeader) {
		mHeader = aHeader;
	}

	/**
	 * @return	Page footer {@link String} if set, {@code null} otherwise.
	 */
	public String getFooter() {
		return mFooter;
	}

	/**
	 * Sets page footer
	 * @param aFooter	{@link String} of footer text.
	 */
	public void setFooter(String aFooter) {
		mFooter = aFooter;
	}
	

	/**
	 * Sets font size in pt, default is 12pt.
	 * @param mFontSize	{@link Integer} font size in pt.
	 */
	public void setFontSize(int aFontSize){
		mFontSize = aFontSize;
	}	
	
	/**
	 * @return	Page text font size in pt.
	 */
	public int getFontSize(){
		return mFontSize;
	}
	

	/**
	 * @return	Page text font type. Default: {@link PDType1Font}.HELVETICA
	 */
	public PDFont getFont() {
		return mFont;
	}

	/**
	 * Sets page text font type.
	 * @param aFont	Page text font type (see {@link PDType1Font}).
	 */
	public void setFont(PDFont aFont) {
		mFont = aFont;
	}

	/**
	 * @return	Page header font size.
	 */
	public int getHeaderFontSize() {
		return mHeaderFontSize;
	}

	/**
	 * Sets page header font size, default is 20pt.
	 * @param aHeaderFontSize	Page header font size in pt.
	 */
	public void setHeaderFontSize(int aHeaderFontSize) {
		mHeaderFontSize = aHeaderFontSize;
	}

	/**
	 * @return	Page header font type (see {@link PDType1Font}).
	 */
	public PDFont getHeaderFont() {
		return mHeaderFont;
	}

	/**
	 * Sets page header font type. Default: {@link PDType1Font}.HELVETICA_BOLD
	 * @param aHeaderFont	Page header font type (see {@link PDType1Font}).
	 */
	public void setHeaderFont(PDFont aHeaderFont) {
		mHeaderFont = aHeaderFont;
	}

	/**
	 * @return	Page footer font size in pt
	 */
	public int getFooterFontSize() {
		return mFooterFontSize;
	}

	/**
	 * Sets page footer font size, default is 10pt. 
	 * @param aFooterFontSize
	 */
	public void setFooterFontSize(int aFooterFontSize) {
		mFooterFontSize = aFooterFontSize;
	}

	/**
	 * @return	Page footer font type (see {@link PDType1Font}).
	 */
	public PDFont getFooterFont() {
		return mFooterFont;
	}

	/**
	 * Sets page footer font type. Default: {@link PDType1Font}.HELVETICA
	 * @param aFooterFont	Page footer font type (see {@link PDType1Font}).
	 */
	public void setFooterFont(PDFont aFooterFont) {
		mFooterFont = aFooterFont;
	}

	/**
	 * @return Page top border in mm.
	 */
	public float getBorderTop() {
		return (mBorderTop * 25.4f) / 72.0f;
	}

	/**
	 * Sets page top border.
	 * @param borderTop	Top border in m.  Default: 20mm.
	 */
	public void setBorderTop(float borderTop) {
		mBorderTop = (borderTop * 72.0f) / 25.4f;
	}

	/**
	 * @return Page bottom border in mm.
	 */
	public float getBoderBottom() {
		return (mBoderBottom * 25.4f) / 72.0f;
	}

	/**
	 * Sets page bottom border.
	 * @param boderBottom	Page bottom border in mm. Default: 20mm.
	 */
	public void setBoderBottom(int boderBottom) {
		mBoderBottom = (boderBottom * 72.0f) / 25.4f;
	}

	/**
	 * @return Page left border in m.
	 */
	public float getBorderLeft() {
		return (mBorderLeft * 25.4f) / 72.0f;
	}

	/**
	 * Sets page left border.
	 * @param borderLeft	Page left border in mm. Default: 20mm.
	 */
	public void setBorderLeft(int borderLeft) {
		mBorderLeft = (borderLeft * 72.0f) / 25.4f;
	}
	
	/**
	 * Sets all borders to the same size.
	 * @param border	Page borders in mm. Default: 20mm.
	 */
	public void setBorders(int border){
		setBorderLeft(border);
		setBoderBottom(border);
		setBorderTop(border);
	}

	/**
	 * @return {@link PDRectangle} page format.
	 */
	public PDRectangle getPageFormat() {
		return mPageFormat;
	}

	/**
	 * Sets page format.
	 * @param pageFormat	{@link PDRectangle} page format. Default is {@link PDRectangle}.A4
	 */
	public void setPageFormat(PDRectangle pageFormat) {
		this.mPageFormat = pageFormat;
	}
	
}
