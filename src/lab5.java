import mpi.*;

import java.util.Scanner;


public class lab5 {



    public static void main(String[] args){

        MPI.Init(args);
        Integer rank=MPI.COMM_WORLD.Rank();
        Integer size=MPI.COMM_WORLD.Size();
        int n,m;


        if (rank==0){

            int [][] matrix = new int [][] {{1,2,3},{4,5,6},{7,8,9}};
            int[][] matrix_rec = new int[3][3];
            n=3;
            m=3;
            System.out.println("root \n enter the matrix");


            /*
            Scanner S = new Scanner(System.in);
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {

                        matrix[i][j] =  S.nextInt();
                        System.out.println(" \n matrix is read item "+matrix[i][j]);
                    }
                }
                S.close();
                System.out.println(" \n matrix is read");
 */
              // MPI.COMM_WORLD.Gather(sendbuf,0,2,MPI.INT,recvbuf,0,2,MPI.INT,root);

                printmatrix(matrix, n, m);

            MPI.COMM_WORLD.Bcast(matrix, 0, 3, MPI.OBJECT, 0) ;
            int [] arr= new int[5];
            MPI.COMM_WORLD.Gather(arr,0,2,MPI.INT,3,0,2,MPI.INT,0);

        }
            else {

            int [][] matrix = new int [3][3];

            Object rec = new Object();

            MPI.COMM_WORLD.Bcast(matrix, 0, 3, MPI.OBJECT, 0) ;
            System.out.println("child");

           // matrix = (int [][])rec;
            printmatrix(matrix,3,3);
                    int[] arr =new int[1];
            int [] dummy = new int [size];
                if (rank==1)
                {
                    arr[0]= matrix[0][0]*(matrix[1][1]*matrix[2][2]-matrix[1][2]*matrix[2][1]);

                 }
                else if (rank==2){
                    arr[0]=  -matrix[0][1]*(matrix[1][0]*matrix[2][2]-matrix[1][2]*matrix[2][0]);
                }
                else {
                    arr[0]= matrix[0][2]*(matrix[1][0]*matrix[2][1]-matrix[1][1]*matrix[2][0]);
                }
            MPI.COMM_WORLD.Gather(arr,0,1,MPI.INT,dummy,0,1,MPI.INT,0);

            //MPI.COMM_WORLD.Gather(matrix_rec,0,2,MPI.INT,dummy,0,2,MPI.INT,root);

        }



            }



    public static void printmatrix(int [][] x,int n,int m){

        for (int i=0;i<n;i++){
            for (int j=0;j<m;j++){
                System.out.print(x[i][j]+" ");

            }
            System.out.println();
        }
    }



}
