/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cookiework.encryptedvideoview2.encryption;
import android.content.Context;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.Integers;
import org.spongycastle.util.encoders.UrlBase64;

import cookiework.encryptedvideoview2.util.DBHelper;
import cookiework.encryptedvideoview2.util.ViewerTag;

import static cookiework.encryptedvideoview2.Constants.ECCURVE_NAME;
import static cookiework.encryptedvideoview2.Constants.TIME_LOG_TAG;

/**
 *
 * @author Andrew
 *
 * This class is designed encapsulate methods that are used in the subscription process.
 * Subscribe, Approve and Finalize.  It uses the methods from PtwittEnc to accomplish
 * this task and provide the Firefox extension an simplier way to do different subscription methods.
 */
public class SubscriptionProcessor {

    private PtWittEnc enc;
    private DBHelper dbHelper;

    public PtWittEnc getEnc() {
        return enc;
    }

    //Constructor generates random 'r' value from a using the java secruity secure random class with specified number of bits
    public SubscriptionProcessor(Context context)
    {
        this.enc = new PtWittEnc(context);
        this.dbHelper = new DBHelper(context);
    }

    //This method prepares a subscription request and takes input for e, N and the tag value
    //and returns the ‘M’ value that is later sent to the destination client for approval.
    public BigInteger generateRequest(BigInteger e, BigInteger N, String tag, BigInteger r)
    {
        ////////////////////////////////////////////////////////////////
        /////////////////         PART ONE   ///////////////////////////
        //Do the Following Action: M = Sha1(Tag) * ((r^e) mod N) mod N//
        ////////////////////////////////////////////////////////////////

        BigInteger M = null;

        try
        {
            long beginTime = System.currentTimeMillis();

            MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.update(tag.getBytes());
            byte[] result = digest.digest();
            BigInteger sha1Final = new BigInteger(1, result); //SHA1 Portion
            //int num = 65;
            BigInteger arr = r;
            //BigInteger arr = BigInteger.valueOf(num);

            BigInteger res= arr.modPow(e, N);
            M = (sha1Final.multiply(res)).mod(N);

            long endTime = System.currentTimeMillis();
            Log.i(TIME_LOG_TAG, "generateRequest(): " + (endTime - beginTime));
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
        return M;
    }

    //This method is a helper function that allows generateRequest to be called
    //using string values instead of BigInteger
    public String getRequestString(String eStr, String NStr, String tag, String rStr)
    {
        BigInteger e = new BigInteger(eStr);
        BigInteger N = new BigInteger(NStr);
        BigInteger r = new BigInteger(rStr);

        BigInteger M = generateRequest(e, N, tag, r);
        String mStr = new String(UrlBase64.encode(M.toByteArray()));
        return mStr;
    }

    //This method does the finalize action, after a subscription is approved the initial user
    //will finalize the subscription.
    public String processResponse(BigInteger mPrime, BigInteger R, BigInteger N, int id)
    {
        ////////////////////////////////////////////////////////////////
        /////////////////         PART THREE   /////////////////////////
        //Do the Following Action: T* = MD5(((m'/R) mod N) || '1') /////
        ////////////////////////////////////////////////////////////////
        long beginTime = System.currentTimeMillis();

        BigInteger sigma = (mPrime.multiply(R.modInverse(N))).mod(N);
        sigmaToFile(sigma, id);
        byte[] temp = sigma.toByteArray();
        String tStar = enc.createTStar(temp);

        long endTime = System.currentTimeMillis();
        Log.i(TIME_LOG_TAG, "processResponse(): " + (endTime - beginTime));

        return tStar;
    }

    //This is a helper function that allows process response to be called with String instead of BigInteger
    public String processResponseString(String mPrimeStr, String RStr, String NStr, int id)
    {
        BigInteger mPrime = null;
        BigInteger R = null;
        BigInteger N = null;

        R = new BigInteger(RStr);
        N = new BigInteger(NStr);
        mPrime = new BigInteger(UrlBase64.decode(mPrimeStr));

        return processResponse(mPrime, R, N, id);
    }

    public void sigmaToFile(BigInteger sigma, int id)
    {
        ViewerTag tag = dbHelper.getTagItem(id);
        tag.setSigma(sigma.toString());
        dbHelper.updateTagItem(tag);
    }
}
