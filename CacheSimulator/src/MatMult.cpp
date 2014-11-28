#include <iostream>
#include <cstdlib>
using namespace std;

void allocatememory_matrix( int**&,int);
void populate_matrix( int**&,int);
void populate_zeromatrix( int**&,int);
void multiply_matrices( int**, int**, int**&,int);
void display_matrix( int**,int );


int  main(int argc, char* argv[]){
	srand(time(NULL));
	int n;
	n = 512;
       	int **A,**B,**C;
	allocatememory_matrix(A,n);
	allocatememory_matrix(B,n);
	allocatememory_matrix(C,n);
	populate_matrix(A,n);
	populate_matrix(B,n);
	multiply_matrices(A,B,C,n);
	//display_matrix(C,n);

}

void  allocatememory_matrix( int**& matrix,int n){
	matrix= new  int*[n];
	for (int i=0;i<n;i++)
		matrix[i]= new  int[n];
	return;
}

void   populate_matrix( int **&matrix,int n){
	for(int i=0;i<n;i++)
		for(int j=0;j<n;j++)
			matrix[i][j]=rand()%10;  //10 is chosen arbitrarily

	return;
}


void multiply_matrices( int** A, int** B, int**& C,int n){
	for(int i=0;i<n;i++)
		for(int j=0;j<n;j++){
                        C[i][j]=0;
			for(int k=0;k<n;k++)
				C[i][j]+=A[i][k]*B[k][j];
                }    
	return;
}

void display_matrix( int **matrix,int n){
	for(int i=0;i<n;i++){
		for(int j=0;j<n;j++)
			cout<<matrix[i][j]<<" ";
		cout<<endl;
	}
}
