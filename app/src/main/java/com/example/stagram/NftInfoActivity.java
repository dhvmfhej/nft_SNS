package com.example.stagram;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class NftInfoActivity extends AppCompatActivity {
    String Link = "";
    BigInteger KLAY_MAX = new BigInteger("10000000000");
    BigInteger NFC_MAX = new BigInteger("9000000000000");
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    HashMap<String, String> keyTokenNumSell = new HashMap<>();
    HashMap<String, String> keyTokenNumNormal = new HashMap<>();
    HashMap<String, PostingItem> tokenNumPostingItemMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
//        BigDecimal balance = NFC_MAX;
//        BigDecimal div = balance.divide(KLAY_MAX).divide(new BigDecimal(2)).setScale(2, BigDecimal.ROUND_FLOOR);
//        BigDecimal a = div.multiply(new BigDecimal("1"));
//        Toast.makeText(NftInfoActivity.this, a.toString(), Toast.LENGTH_SHORT).show();

        ref.child("SellPost").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    PostingItem post=data.getValue(PostingItem.class);
                    keyTokenNumSell.put(post.getUserDetail(), data.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("NormalPost").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    PostingItem post=data.getValue(PostingItem.class);
                    keyTokenNumNormal.put(post.getUserDetail(), data.getKey());
                    tokenNumPostingItemMap.put(post.getUserDetail(), post);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.nft_info);
        Intent intent = getIntent();

        Blockchain b = new Blockchain();
        ImageFile imageFile = new ImageFile();
        String time = intent.getExtras().getString("time");
        String tokenID = intent.getExtras().getString("userDetail");
        String price = intent.getExtras().getString("price");
        String userId = intent.getExtras().getString("userName");
        String priceToToken = intent.getExtras().getString("tokenPrice");

        String hexString = null;
        System.out.print(time+userId);

        try {
            if (tokenID.equals("NOT NFT")){
                TextView textView = findViewById(R.id.textViewImgContents);
                textView.setText("DB??? ????????? ??????");
                hexString = intent.getExtras().getString("img");
            }
            else{
                hexString = b.get_NFT_info(Integer.valueOf(tokenID));
                TextView tokenPriceTextView = findViewById(R.id.tokenPriceTextView);
                tokenPriceTextView.setText("NFT ??????: " + price+" (KLAY)");
                TextView tokenPriceTextView2 = findViewById(R.id.tokenPriceTextView2);
                tokenPriceTextView2.setText("NFT ??????: " + priceToToken+" (NFC)");
                TextView contractTextView = findViewById(R.id.contractTextView);
                contractTextView.setText("???????????? ??????: " + b.contract_address);
                TextView tokenIdTextView = findViewById(R.id.tokenIdTextView) ;
                tokenIdTextView.setText("?????? ID: " + tokenID);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Link = "https://baobab.scope.klaytn.com/nft/";
        Link += b.contract_address + "/" + tokenID; //?????? ????????? ??? NFT??? ?????? ????????? ??????.
        if (hexString != null){
            Bitmap bitmap = imageFile.hexStringToBitmap(hexString);
            ImageView img = findViewById(R.id.NFT_image);
            img.setImageBitmap(bitmap);
        }

        TextView userIdTextView = findViewById(R.id.userIdTextView) ;
        userIdTextView.setText("????????? : " + userId);
        Button copyButton = findViewById(R.id.copyButton);
        TextView info = findViewById(R.id.info);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent infoIntent = new Intent(NftInfoActivity.this,tutorialActivity.class);
                startActivity(infoIntent);
            }
        });

        copyButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", userId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(NftInfoActivity.this, "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

            }
        });


        Button button1 = findViewById(R.id.explorerButton);
        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(Link));
                startActivity(intentUrl);
            }
        });
        //???????????? ?????? ??? ????????? ??????
        Button purchaseButton = findViewById(R.id.purchaseButton);
        Button purchaseButtonToken = findViewById(R.id.purchaseButtonToken);

        //????????? ???????????? ???????????? ????????????
        if(intent.getExtras().getString("isToken").equals("False"))
            purchaseButton.setVisibility(View.GONE);

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String privateKey = ( (LoginInfo) getApplication() ).getPrivateKey();
                String address = ( (LoginInfo) getApplication() ).getAddress();
                String memo = "NFT ?????????: " + userId+ " NFT ?????????: " + address + " ?????? : " + priceToToken +"KLAY";
                //????????? ???????????? ?????? ?????????.
                try {
                    b.send_KLAY(privateKey, userId,memo, price);
                } catch (Exception e) {
                    Toast.makeText(NftInfoActivity.this, "KLAY ?????? ??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    b.send_NFT(b.private_key, address, Integer.parseInt(tokenID));
                } catch (Exception e) {
                    Toast.makeText(NftInfoActivity.this, "NFT ?????? ??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    //????????? ?????? ????????? ??????.
                    // ????????? = ??????(KLAY) * (?????? ????????? / KLAY ??? ?????????) -- ????????? ??? ????????? ???????????? ?????? ??????.
                    //String result = div.multiply(new BigInteger(price)).toString();
                    b.send_Token(b.private_key, address, "1"); //????????? ??????
                    //b.send_Token(b.private_key, userId, result); //????????? ??????.
                } catch (Exception e) {
                    Toast.makeText(NftInfoActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                }
                delete_db(tokenID, "SellPost");
                modify_db(tokenID, address, "NormalPost");
                finish(); //??? ?????? ??????????????? ????????? ????????? ??????.
            }
        });

        purchaseButtonToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String privateKey = ( (LoginInfo) getApplication() ).getPrivateKey();
                String address = ( (LoginInfo) getApplication() ).getAddress();
                //????????? ???????????? ?????? ?????????.
                try {
                    b.send_Token(privateKey, userId, priceToToken);
                } catch (Exception e) {
                    Toast.makeText(NftInfoActivity.this, "NFC ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    b.send_NFT(b.private_key, address, Integer.parseInt(tokenID));
                } catch (Exception e) {
                    Toast.makeText(NftInfoActivity.this, "NFT ?????? ??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                delete_db(tokenID, "SellPost");
                modify_db(tokenID, address, "NormalPost");
                finish(); //??? ?????? ??????????????? ????????? ????????? ??????.
            }
        });
    }

    private void delete_db(String tokenNum, String dbName){
        HashMap<String, String> map = new HashMap<>();
        if (dbName.equals("NormalPost"))
            map = keyTokenNumNormal;
        else
            map = keyTokenNumSell;
        database.getReference().child(dbName).child(Objects.requireNonNull(map.get(tokenNum))).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NftInfoActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NftInfoActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void modify_db(String tokenNum, String newAddress, String dbName){
        PostingItem post = tokenNumPostingItemMap.get(tokenNum);
        delete_db(tokenNum,dbName);
        post.setPostUser(newAddress); //????????? ????????? ?????? db??? ???????????? ???.
        DatabaseReference userRef = ref.child(dbName);
        userRef.child(post.getPostUser()+post.getPostedDate()).setValue(post);
    }
}